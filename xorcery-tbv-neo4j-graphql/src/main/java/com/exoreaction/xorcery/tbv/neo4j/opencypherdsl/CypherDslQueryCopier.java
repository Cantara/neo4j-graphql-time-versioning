package com.exoreaction.xorcery.tbv.neo4j.opencypherdsl;

import org.neo4j.cypherdsl.core.Condition;
import org.neo4j.cypherdsl.core.ExposesMatch;
import org.neo4j.cypherdsl.core.ExposesReturning;
import org.neo4j.cypherdsl.core.ExposesSubqueryCall;
import org.neo4j.cypherdsl.core.Expression;
import org.neo4j.cypherdsl.core.Match;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.PatternElement;
import org.neo4j.cypherdsl.core.RelationshipPattern;
import org.neo4j.cypherdsl.core.ResultStatement;
import org.neo4j.cypherdsl.core.Return;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.StatementBuilder;
import org.neo4j.cypherdsl.core.Subquery;
import org.neo4j.cypherdsl.core.Where;
import org.neo4j.cypherdsl.core.With;
import org.neo4j.cypherdsl.core.ast.Visitable;
import org.neo4j.cypherdsl.core.internal.ReflectiveVisitor;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class CypherDslQueryCopier extends ReflectiveVisitor {

    private Statement statementCopy;
    private int depth = 0;

    public Statement getStatementCopy() {
        return statementCopy;
    }

    @Override
    protected boolean preEnter(Visitable visitable) {
        depth++;
        System.out.printf("%s%s%n", "| ".repeat(depth), visitable.getClass().getName());
        return true;
    }

    @Override
    protected void postLeave(Visitable visitable) {
        depth--;
    }

    static class StatementContext implements SubqueryOperandAware, WithOperandAware, MatchOperandAware, ReturnOperandAware {

        final List<Object> clauses = new LinkedList<>();
        final List<SubqueryContext> subqueryContexts = new LinkedList<>();
        final List<WithContext> withContexts = new LinkedList<>();
        final List<MatchContext> matchContexts = new LinkedList<>();
        ReturnContext returnContext;

        @Override
        public StatementContext addMatchContext(MatchContext matchContext) {
            clauses.add(matchContext);
            matchContexts.add(matchContext);
            return this;
        }

        @Override
        public StatementContext withReturnContext(ReturnContext returnContext) {
            clauses.add(returnContext);
            this.returnContext = returnContext;
            return this;
        }

        @Override
        public StatementContext addWithContext(WithContext withContext) {
            clauses.add(withContext);
            withContexts.add(withContext);
            return this;
        }

        @Override
        public StatementContext addSubqueryContext(SubqueryContext subqueryContext) {
            clauses.add(subqueryContext);
            subqueryContexts.add(subqueryContext);
            return this;
        }
    }

    static class MatchContext implements PatternElementOperandAware, WhereOperandAware {
        final boolean isOptional;

        final List<PatternElement> patternElements = new LinkedList<>();

        Condition condition;

        MatchContext(boolean isOptional) {
            this.isOptional = isOptional;
        }

        @Override
        public MatchContext withWhereCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        @Override
        public PatternElementOperandAware addPatternElement(PatternElement patternElement) {
            patternElements.add(patternElement);
            return this;
        }
    }

    static class SubqueryContext implements StatementOperandAware {
        Statement statement;

        @Override
        public StatementOperandAware addStatement(Statement statement) {
            if (this.statement != null) {
                throw new CypherDslQueryTransformerException();
            }
            this.statement = statement;
            return this;
        }
    }

    static class WithContext implements ExpressionOperandAware {

        private final List<Expression> expressions = new LinkedList<>();

        @Override
        public WithContext addExpressionElement(Expression expression) {
            this.expressions.add(expression);
            return this;
        }
    }

    interface SubqueryOperandAware {
        SubqueryOperandAware addSubqueryContext(SubqueryContext subqueryContext);
    }

    interface ConditionOperandAware {
        ConditionOperandAware withCondition(Condition condition);
    }

    interface MatchOperandAware {
        MatchOperandAware addMatchContext(MatchContext matchContext);
    }

    interface WhereOperandAware {
        WhereOperandAware withWhereCondition(Condition condition);
    }

    interface PatternElementOperandAware {
        PatternElementOperandAware addPatternElement(PatternElement patternElement);
    }

    interface ReturnOperandAware {
        ReturnOperandAware withReturnContext(ReturnContext returnContext);
    }

    interface StatementOperandAware {
        StatementOperandAware addStatement(Statement statement);
    }

    interface WithOperandAware {
        WithOperandAware addWithContext(WithContext withContext);
    }

    interface ExpressionOperandAware {
        ExpressionOperandAware addExpressionElement(Expression expression);
    }

    static class WhereContext implements ConditionOperandAware {
        Condition condition;

        @Override
        public WhereContext withCondition(Condition condition) {
            this.condition = condition;
            return this;
        }
    }

    static class ConditionContext {
    }

    static class ReturnContext implements ExpressionOperandAware {
        final List<Expression> expressions = new LinkedList<>();

        @Override
        public ReturnContext addExpressionElement(Expression expression) {
            expressions.add(expression);
            return this;
        }
    }

    private final Deque<Object> operatorStack = new LinkedList<>();

    void enter(Statement statement) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), statement.getClass().getSimpleName());
        operatorStack.push(new StatementContext());
    }

    void leave(Statement statement) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), statement.getClass().getSimpleName());
        StatementContext statementContext = (StatementContext) operatorStack.pop();
        StatementBuilder statementBuilder = Statement.builder();
        ExposesMatch exposesMatch = statementBuilder;
        StatementBuilder.ExposesWith exposesWith = null;
        ExposesSubqueryCall exposesSubqueryCall = statementBuilder;
        ExposesReturning exposesReturning = statementBuilder;
        for (Object clause : statementContext.clauses) {
            if (clause instanceof WithContext withContext) {
                StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere orderableOngoingReadingAndWithWithoutWhere;
                if (exposesWith == null) {
                    orderableOngoingReadingAndWithWithoutWhere = statementBuilder.with(withContext.expressions);
                } else {
                    orderableOngoingReadingAndWithWithoutWhere = exposesWith.with(withContext.expressions);
                }
                exposesWith = orderableOngoingReadingAndWithWithoutWhere;
                exposesMatch = orderableOngoingReadingAndWithWithoutWhere;
                exposesSubqueryCall = orderableOngoingReadingAndWithWithoutWhere;
                exposesReturning = orderableOngoingReadingAndWithWithoutWhere;
            } else if (clause instanceof SubqueryContext subqueryContext) {
                StatementBuilder.OngoingReadingWithoutWhere ongoingReadingWithoutWhere = exposesSubqueryCall.call(subqueryContext.statement);
                exposesWith = ongoingReadingWithoutWhere;
                exposesMatch = ongoingReadingWithoutWhere;
                exposesSubqueryCall = ongoingReadingWithoutWhere;
                exposesReturning = ongoingReadingWithoutWhere;
            } else if (clause instanceof MatchContext matchContext) {
                StatementBuilder.OngoingReadingWithoutWhere ongoingReadingWithoutWhere = exposesMatch.match(matchContext.isOptional, matchContext.patternElements.toArray(new PatternElement[0]));
                exposesWith = ongoingReadingWithoutWhere;
                exposesMatch = ongoingReadingWithoutWhere;
                exposesSubqueryCall = ongoingReadingWithoutWhere;
                exposesReturning = ongoingReadingWithoutWhere;
                if (matchContext.condition != null) {
                    StatementBuilder.OngoingReadingWithWhere ongoingReadingWithWhere = ongoingReadingWithoutWhere.where(matchContext.condition);
                    exposesWith = ongoingReadingWithWhere;
                    exposesMatch = ongoingReadingWithWhere;
                    exposesSubqueryCall = ongoingReadingWithWhere;
                    exposesReturning = ongoingReadingWithWhere;
                }
            } else if (clause instanceof ReturnContext returnContext) {
                // handled at end after clauses loop
            }
        }
        if (statementContext.returnContext == null) {
            throw new CypherDslQueryTransformerException();
        }
        StatementBuilder.OngoingReading ongoingReading = (StatementBuilder.OngoingReading) exposesMatch;
        StatementBuilder.OngoingReadingAndReturn ongoingReadingAndReturn = ongoingReading.returning(statementContext.returnContext.expressions);
        ResultStatement resultStatement = ongoingReadingAndReturn.build();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext == null) {
            statementCopy = resultStatement;
        } else {
            if (ancestorContext instanceof StatementOperandAware statementOperandAware) {
                statementOperandAware.addStatement(resultStatement);
            }
        }
    }

    void enter(Subquery subquery) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), subquery.getClass().getSimpleName());
        operatorStack.push(new SubqueryContext());
    }

    void leave(Subquery subquery) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), subquery.getClass().getSimpleName());
        SubqueryContext subqueryContext = (SubqueryContext) operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof SubqueryOperandAware subqueryOperandAware) {
            subqueryOperandAware.addSubqueryContext(subqueryContext);
        }
    }

    void enter(With with) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), with.getClass().getSimpleName());
        operatorStack.push(new WithContext());
    }

    void leave(With with) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), with.getClass().getSimpleName());
        WithContext withContext = (WithContext) operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof WithOperandAware withOperandAware) {
            withOperandAware.addWithContext(withContext);
        }
    }

    void enter(Return returning) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), returning.getClass().getSimpleName());
        operatorStack.push(new ReturnContext());
    }

    void leave(Return returning) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), returning.getClass().getSimpleName());
        ReturnContext returnContext = (ReturnContext) operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof ReturnOperandAware returnOperandAware) {
            returnOperandAware.withReturnContext(returnContext);
        }
    }

    void enter(Expression expression) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), expression.getClass().getSimpleName());
        operatorStack.push(new Object());
    }

    void leave(Expression expression) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), expression.getClass().getSimpleName());
        operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof ExpressionOperandAware expressionOperandAware) {
            expressionOperandAware.addExpressionElement(expression);
        }
    }

    void enter(Match match) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), match.getClass().getSimpleName());
        operatorStack.push(new MatchContext(match.isOptional()));
    }

    void leave(Match match) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), match.getClass().getSimpleName());
        MatchContext matchContext = (MatchContext) operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof MatchOperandAware matchOperandAware) {
            matchOperandAware.addMatchContext(matchContext);
        }
    }

    void enter(PatternElement patternElement) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), patternElement.getClass().getSimpleName());
        operatorStack.push(new Object());
    }

    void leave(PatternElement patternElement) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), patternElement.getClass().getSimpleName());
        operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof PatternElementOperandAware patternElementOperandAware) {
            patternElementOperandAware.addPatternElement(patternElement);
        }
    }

    void enter(Node node) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), node.getClass().getSimpleName());
        operatorStack.push(new Object());
    }

    void leave(Node node) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), node.getClass().getSimpleName());
        operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof PatternElementOperandAware patternElementOperandAware) {
            patternElementOperandAware.addPatternElement(node);
        }
    }

    void enter(RelationshipPattern relationshipPattern) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), relationshipPattern.getClass().getSimpleName());
        operatorStack.push(new Object());
    }

    void leave(RelationshipPattern relationshipPattern) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), relationshipPattern.getClass().getSimpleName());
        operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof PatternElementOperandAware patternElementOperandAware) {
            patternElementOperandAware.addPatternElement(relationshipPattern);
        }
    }

    void enter(Where where) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), where.getClass().getSimpleName());
        operatorStack.push(new WhereContext());
    }

    void leave(Where where) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), where.getClass().getSimpleName());
        WhereContext whereContext = (WhereContext) operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof WhereOperandAware whereOperandAware) {
            whereOperandAware.withWhereCondition(whereContext.condition);
        }
    }

    void enter(Condition condition) {
        System.out.printf("%sENTER %s%n", "| ".repeat(depth), condition.getClass().getSimpleName());
        operatorStack.push(new ConditionContext());
    }

    void leave(Condition condition) {
        System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), condition.getClass().getSimpleName());
        ConditionContext conditionContext = (ConditionContext) operatorStack.pop();
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext instanceof ConditionOperandAware conditionOperandAware) {
            conditionOperandAware.withCondition(condition);
        }
    }
}
