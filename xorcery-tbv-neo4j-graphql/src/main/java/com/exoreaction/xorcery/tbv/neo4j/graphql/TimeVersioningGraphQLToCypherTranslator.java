package com.exoreaction.xorcery.tbv.neo4j.graphql;

import graphql.schema.GraphQLSchema;
import org.neo4j.graphql.Cypher;
import org.neo4j.graphql.OptimizedQueryException;
import org.neo4j.graphql.QueryContext;
import org.neo4j.graphql.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            cyphers = translator.translate(query, parameters, queryContext);
        } catch (OptimizedQueryException e) {
            throw new RuntimeException(e);
        }
        List<Cypher> result = new ArrayList<>(cyphers.size());
        for (Cypher cypher : cyphers) {
            LinkedHashMap<String, Object> transformedComponent2 = new LinkedHashMap<>(cypher.component2());
            transformedComponent2.put(TBVCypherConstants.PARAMETER_IDENTIFIER_TIME_BASED_VERSION, timeVersion);
            String transformedComponent1;
            LOG.trace("CYPHER BEFORE: {}", cypher.component1());
            transformedComponent1 = new CypherQueryTransformer(domains).transform(cypher.component1());
            LOG.trace("CYPHER AFTER: {}", transformedComponent1);
            result.add(new Cypher(transformedComponent1, transformedComponent2, cypher.component3(), cypher.component4()));
        }
        return result;
    }
}
