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

public class TimeVersioningCypherDslQueryTransformer extends GenericCypherDslQueryTransformer {

    public TimeVersioningCypherDslQueryTransformer(boolean debug) {
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
        String matchCypher = renderCypherFor(operatorStack, matchContext.originalMatch);

        Pattern pattern = Pattern.compile("MATCH(.*)WHERE(.*)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(matchCypher);
        String transformed;
        if (m.matches()) {
            String patternMatch = m.group(1);
            String whereMatch = m.group(2);

            transformed = "MATCH " + modifyMatchPattern(patternMatch) + " WHERE " + modifyWhereCondition(whereMatch);
        } else {
            // attempt to match without where clause
            Pattern nonWherePattern = Pattern.compile("MATCH(.*)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher nonWhereMatcher = nonWherePattern.matcher(matchCypher);
            if (!nonWhereMatcher.matches()) {
                throw new CypherDslQueryTransformerException();
            }
            String patternMatch = nonWhereMatcher.group(1);
            transformed = "MATCH " + modifyMatchPattern(patternMatch) + " WHERE " + modifyWhereCondition(null);
        }

        Match transformedMatch = (Match) CypherParser.parseClause(transformed);

        GenericCypherDslQueryTransformer innerTransformer = new GenericCypherDslQueryTransformer(debug);
        transformedMatch.accept(innerTransformer);
        MatchContext transformedMatchContext = (MatchContext) innerTransformer.getOutput();

        return transformedMatchContext;
    }

    private String renderCypherFor(Deque<Object> operatorStack, Match originalMatch) {
        StatementContext statementContext = (StatementContext) operatorStack.iterator().next();
        PrettyPrintingVisitor renderingVisitor = new PrettyPrintingVisitor(statementContext.originalStatement.getContext(), Configuration.prettyPrinting());
        originalMatch.accept(renderingVisitor);
        String matchCypher = renderingVisitor.getRenderedContent();
        return matchCypher;
    }

    private String modifyMatchPattern(String inputPattern) {
        Matcher m = Pattern.compile("\\s*\\(([^:]*):([^)]*)\\)\\s*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(inputPattern);
        if (!m.matches()) {
            throw new CypherDslQueryTransformerException("CYPHER: " + inputPattern + "\ndoes not match pattern.");
        }
        String nodeIdentifierLiteral = m.group(1);
        String nodeTypeLiteral = m.group(2);
        String modifiedResult = String.format("(_r:%s_R)<-[_v:VERSION_OF]-(%s)", nodeTypeLiteral, nodeIdentifierLiteral);
        return modifiedResult;
    }

    String modifyWhereCondition(String inputCondition) {
        return "(_v.from <= $" + TBVCypherConstants.PARAMETER_IDENTIFIER_TIME_BASED_VERSION
                + " AND coalesce($" + TBVCypherConstants.PARAMETER_IDENTIFIER_TIME_BASED_VERSION + " < _v.to, true))"
                + (inputCondition == null ? "" : " AND (" + inputCondition + ")");
    }

}
