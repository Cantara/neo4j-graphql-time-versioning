package com.exoreaction.xorcery.tbv.neo4j.opencypherdsl;

import com.exoreaction.xorcery.tbv.neo4j.cypher.TBVCypherConstants;
import com.exoreaction.xorcery.tbv.neo4j.opencypherdsl.render.Configuration;
import com.exoreaction.xorcery.tbv.neo4j.opencypherdsl.render.PrettyPrintingVisitor;
import org.neo4j.cypherdsl.core.Match;
import org.neo4j.cypherdsl.parser.CypherParser;

import java.util.Deque;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeVersioningGenericCypherDslQueryTransformer extends GenericCypherDslQueryTransformer {

    public TimeVersioningGenericCypherDslQueryTransformer(boolean debug) {
        super(debug);
        registerTransform(MatchContext.class, this::isTopLevelMatchClause, this::topLevelMatchTransformation);
    }

    private boolean isTopLevelMatchClause(Deque<Object> operatorStack, MatchContext matchContext) {
        Iterator<Object> iterator = operatorStack.iterator();
        if (!iterator.hasNext()) {
            return false;
        }
        Object rootOperatorContext = iterator.next();
        if (rootOperatorContext instanceof StatementContext statementContext) {
            return !iterator.hasNext();
        }
        return false;
    }

    private MatchContext topLevelMatchTransformation(Deque<Object> operatorStack, MatchContext matchContext) {
        StatementContext statementContext = (StatementContext) operatorStack.iterator().next();

        PrettyPrintingVisitor renderingVisitor = new PrettyPrintingVisitor(statementContext.originalStatement.getContext(), Configuration.prettyPrinting());
        matchContext.originalMatch.accept(renderingVisitor);
        String matchCypher = renderingVisitor.getRenderedContent();

        Matcher m = Pattern.compile("MATCH(.*)WHERE(.*)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(matchCypher);
        if (!m.matches()) {
            throw new CypherDslQueryTransformerException();
        }

        String patternMatch = m.group(1);
        String whereMatch = m.group(2);

        System.out.printf("CYPHER: %s%n", matchCypher);

        String newMatch = "MATCH " + patternMatch + " WHERE (_v.from <= $" + TBVCypherConstants.PARAMETER_IDENTIFIER_TIME_BASED_VERSION + " AND coalesce($" + TBVCypherConstants.PARAMETER_IDENTIFIER_TIME_BASED_VERSION + " < _v.to, true)) AND ("
                + whereMatch + ")";

        Match transformedMatch = (Match) CypherParser.parseClause(newMatch);

        GenericCypherDslQueryTransformer innerTransformer = new GenericCypherDslQueryTransformer(debug);
        transformedMatch.accept(innerTransformer);
        MatchContext transformedMatchContext = (MatchContext) innerTransformer.getOutput();

        return transformedMatchContext;
    }
}
