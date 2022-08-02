package com.exoreaction.xorcery.tbv.neo4j.graphqltocypher;

import com.exoreaction.xorcery.tbv.neo4j.cypher.TBVCypherConstants;
import com.exoreaction.xorcery.tbv.neo4j.graphql.TBVGraphQLConstants;
import com.exoreaction.xorcery.tbv.neo4j.opencypherdsl.TimeVersioningCypherDslQueryTransformer;
import graphql.schema.GraphQLSchema;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.neo4j.cypherdsl.parser.CypherParser;
import org.neo4j.graphql.Cypher;
import org.neo4j.graphql.OptimizedQueryException;
import org.neo4j.graphql.QueryContext;
import org.neo4j.graphql.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class TimeVersioningGraphQLToCypherTranslator {

    private static final Logger LOG = LoggerFactory.getLogger(TimeVersioningGraphQLToCypherTranslator.class);

    private final Translator translator;
    private final Set<String> domains;

    public TimeVersioningGraphQLToCypherTranslator(GraphQLSchema graphQlSchema, Set<String> domains) {
        this.translator = new Translator(graphQlSchema);
        this.domains = domains;
    }

    public List<Cypher> translate(String query, Map<String, Object> parameters, ZonedDateTime timeVersion) {
        QueryContext queryContext = new QueryContext();
        List<Cypher> cyphers;
        try {
            Map<String, Object> params = new LinkedHashMap<>(parameters);
            params.put(TBVGraphQLConstants.VARIABLE_IDENTIFIER_TIME_BASED_VERSION, toNeo4jDateTimeMap(timeVersion));
            cyphers = translator.translate(query, params, queryContext);
        } catch (OptimizedQueryException e) {
            throw new RuntimeException(e);
        }
        List<Cypher> result = cyphers.stream()
                .peek(cypher -> LOG.trace("CYPHER BEFORE: \n{}", cypher.component1()))
                .map(cypher -> {
                    String transformedComponent1 = transform(cypher.component1());
                    return new Cypher(transformedComponent1, cypher.component2(), cypher.component3(), cypher.component4());
                })
                .map(cypher -> {
                    String transformedComponent1 = cypher.component1();
                    Map<String, Object> transformedComponent2 = new LinkedHashMap<>(cypher.component2());
                    Iterator<Map.Entry<String, Object>> pit = transformedComponent2.entrySet().iterator();
                    while (pit.hasNext()) {
                        Map.Entry<String, Object> entry = pit.next();
                        if (cypher.component1().contains("$" + entry.getKey() + " AS ver")) {
                            transformedComponent1 = transformedComponent1.replaceAll("\\$" + entry.getKey() + " AS ver", Matcher.quoteReplacement("$" + TBVCypherConstants.PARAMETER_IDENTIFIER_TIME_BASED_VERSION + " AS ver"));
                            pit.remove();
                        }
                    }
                    transformedComponent2.put(TBVCypherConstants.PARAMETER_IDENTIFIER_TIME_BASED_VERSION, timeVersion);
                    return new Cypher(transformedComponent1, transformedComponent2, cypher.component3(), cypher.component4());
                })
                .peek(cypher -> LOG.trace("CYPHER AFTER: \n{}", cypher.component1()))
                .collect(Collectors.toList());
        return result;
    }

    private Map<String, Object> toNeo4jDateTimeMap(ZonedDateTime input) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("year", input.getYear());
        output.put("month", input.getMonthValue());
        output.put("day", input.getDayOfMonth());
        output.put("hour", input.getHour());
        output.put("minute", input.getMinute());
        output.put("second", input.getSecond());
        output.put("millisecond", input.getNano() / 1_000_000);
        output.put("microsecond", (input.getNano() % 1_000_000) / 1_000);
        output.put("nanosecond", input.getNano() % 1_000);
        output.put("timezone", input.getZone().getId());
        output.put("formatted", "");
        return output;
    }

    private String transform(String cypher) {
        Statement statement = CypherParser.parse(cypher);
        TimeVersioningCypherDslQueryTransformer transformer = new TimeVersioningCypherDslQueryTransformer(false);
        statement.accept(transformer);
        Statement transformedStatement = (Statement) transformer.getOutput();
        Renderer renderer = Renderer.getRenderer(Configuration.prettyPrinting());
        String transformedCypher = renderer.render(transformedStatement);
        return transformedCypher;
    }
}
