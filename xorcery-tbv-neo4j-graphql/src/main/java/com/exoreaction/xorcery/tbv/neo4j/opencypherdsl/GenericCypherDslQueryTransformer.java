package com.exoreaction.xorcery.tbv.neo4j.opencypherdsl;

import org.neo4j.cypherdsl.core.AliasedExpression;
import org.neo4j.cypherdsl.core.Condition;
import org.neo4j.cypherdsl.core.ExposesCall;
import org.neo4j.cypherdsl.core.ExposesMatch;
import org.neo4j.cypherdsl.core.ExposesReturning;
import org.neo4j.cypherdsl.core.ExposesSubqueryCall;
import org.neo4j.cypherdsl.core.Expression;
import org.neo4j.cypherdsl.core.Literal;
import org.neo4j.cypherdsl.core.Match;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.PatternElement;
import org.neo4j.cypherdsl.core.ProcedureCall;
import org.neo4j.cypherdsl.core.Relationship;
import org.neo4j.cypherdsl.core.RelationshipPattern;
import org.neo4j.cypherdsl.core.ResultStatement;
import org.neo4j.cypherdsl.core.Return;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.StatementBuilder;
import org.neo4j.cypherdsl.core.Subquery;
import org.neo4j.cypherdsl.core.SymbolicName;
import org.neo4j.cypherdsl.core.Where;
import org.neo4j.cypherdsl.core.With;
import org.neo4j.cypherdsl.core.ast.Visitable;
import org.neo4j.cypherdsl.core.internal.ProcedureName;
import org.neo4j.cypherdsl.core.internal.ReflectiveVisitor;
import org.neo4j.cypherdsl.core.internal.YieldItems;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenericCypherDslQueryTransformer extends ReflectiveVisitor {

    protected final OperandAware operandAware;

    protected Object output;

    protected int depth = 0;

    protected boolean debug;

    public GenericCypherDslQueryTransformer(OperandAware operandAware) {
        this.operandAware = operandAware;
    }

    public GenericCypherDslQueryTransformer(OperandAware operandAware, boolean debug) {
        this.operandAware = operandAware;
        this.debug = debug;
    }

    public GenericCypherDslQueryTransformer() {
        this.operandAware = null;
    }

    public GenericCypherDslQueryTransformer(boolean debug) {
        this.operandAware = null;
        this.debug = debug;
    }

    public Object getOutput() {
        return output;
    }

    @Override
    protected boolean preEnter(Visitable visitable) {
        depth++;
        if (debug) {
            System.out.printf("%s%s%n", "| ".repeat(depth), visitable.getClass().getName());
        }
        return true;
    }

    @Override
    protected void postLeave(Visitable visitable) {
        depth--;
    }

    static class StatementContext implements SubqueryOperandAware, WithOperandAware, MatchOperandAware, ReturnOperandAware {

        final Statement originalStatement;

        final List<Object> clauses = new LinkedList<>();
        final List<SubqueryContext> subqueryContexts = new LinkedList<>();
        final List<WithContext> withContexts = new LinkedList<>();
        final List<MatchContext> matchContexts = new LinkedList<>();
        ReturnContext returnContext;

        public StatementContext(Statement originalStatement) {
            this.originalStatement = originalStatement;
        }

        @Override
        public StatementContext add(Object operand) {
            if (operand instanceof MatchContext matchContext) {
                clauses.add(matchContext);
                matchContexts.add(matchContext);
            } else if (operand instanceof ReturnContext returnContext) {
                clauses.add(returnContext);
                this.returnContext = returnContext;
            } else if (operand instanceof WithContext withContext) {
                clauses.add(withContext);
                withContexts.add(withContext);
            } else if (operand instanceof SubqueryContext subqueryContext) {
                clauses.add(subqueryContext);
                subqueryContexts.add(subqueryContext);
            } else if (operand instanceof ProcedureCallContext procedureCallContext) {
                clauses.add(procedureCallContext);
            } else {
                throw new CypherDslQueryTransformerException();
            }
            return this;
        }
    }

    static class MatchContext implements PatternElementOperandAware, WhereOperandAware {
        final Match originalMatch;
        final boolean isOptional;

        final List<PatternElement> patternElements = new LinkedList<>();

        Condition condition;

        MatchContext(Match originalMatch, boolean isOptional) {
            this.originalMatch = originalMatch;
            this.isOptional = isOptional;
        }

        @Override
        public MatchContext add(Object operand) {
            if (operand instanceof Condition condition) {
                this.condition = condition;
            } else if (operand instanceof PatternElement patternElement) {
                patternElements.add(patternElement);
            } else {
                throw new CypherDslQueryTransformerException();
            }
            return this;
        }
    }

    static class SubqueryContext implements StatementOperandAware {
        Statement statement;

        @Override
        public StatementOperandAware add(Object operand) {
            if (this.statement != null) {
                throw new CypherDslQueryTransformerException();
            }
            this.statement = (Statement) operand;
            return this;
        }
    }

    static class ProcedureCallContext implements StatementOperandAware {
        ProcedureName procedureName;
        Statement statement;
        final List<Expression> arguments = new ArrayList<>();
        final List<Expression> yieldExpressions = new ArrayList<>();

        @Override
        public StatementOperandAware add(Object operand) {
            if (operand instanceof ProcedureCall procedureCall) {
                yieldExpressions.addAll(procedureCall.getIdentifiableExpressions());
                return this;
            }
            if (operand instanceof ProcedureName procedureName) {
                this.procedureName = procedureName;
                return this;
            }
            if (operand instanceof Expression expression) {
                arguments.add(expression);
                return this;
            }
            if (operand instanceof Statement statement) {
                if (this.statement != null) {
                    throw new CypherDslQueryTransformerException();
                }
                this.statement = statement;
                return this;
            }
            throw new CypherDslQueryTransformerException("operand type not supported: " + operand.getClass());
        }
    }

    static class WithContext implements ExpressionOperandAware {

        private final List<Expression> expressions = new LinkedList<>();

        @Override
        public WithContext add(Object operand) {
            this.expressions.add((Expression) operand);
            return this;
        }
    }

    interface OperandAware {
        OperandAware add(Object operand);
    }

    interface SubqueryOperandAware extends OperandAware {
    }

    interface ConditionOperandAware extends OperandAware {
    }

    interface MatchOperandAware extends OperandAware {
    }

    interface WhereOperandAware extends OperandAware {
    }

    interface PatternElementOperandAware extends OperandAware {
    }

    interface ReturnOperandAware extends OperandAware {
    }

    interface StatementOperandAware extends OperandAware {
    }

    interface WithOperandAware extends OperandAware {
    }

    interface ExpressionOperandAware extends OperandAware {
    }

    static class WhereContext implements ConditionOperandAware {
        Condition condition;

        @Override
        public WhereContext add(Object operand) {
            this.condition = (Condition) operand;
            return this;
        }
    }

    static class ConditionContext {
    }

    static class ReturnContext implements ExpressionOperandAware {
        final List<Expression> expressions = new LinkedList<>();

        @Override
        public ReturnContext add(Object operand) {
            expressions.add((Expression) operand);
            return this;
        }
    }

    private final Deque<Object> operatorStack = new LinkedList<>();

    void transformAndPushUp(Object operand) {
        Object ancestorContext = operatorStack.peek();
        if (ancestorContext == null) {
            Object transformed = doTransform(operand);
            if (this.operandAware != null) {
                this.operandAware.add(transformed);
            }
            this.output = transformed;
            return;
        }
        if (ancestorContext instanceof OperandAware operandAware) {
            Object transformed = doTransform(operand);
            operandAware.add(transformed);
        }
    }

    void enter(Statement statement) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), statement.getClass().getSimpleName());
        }
        operatorStack.push(new StatementContext(statement));
    }

    void leave(Statement statement) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), statement.getClass().getSimpleName());
        }
        StatementContext statementContext = (StatementContext) operatorStack.pop();
        StatementBuilder statementBuilder = Statement.builder();
        ExposesMatch exposesMatch = statementBuilder;
        StatementBuilder.ExposesWith exposesWith = null;
        ExposesSubqueryCall exposesSubqueryCall = statementBuilder;
        ExposesReturning exposesReturning = statementBuilder;
        StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere orderableOngoingReadingAndWithWithoutWhere = null;
        for (Object clause : statementContext.clauses) {
            if (clause instanceof WithContext withContext) {
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
            } else if (clause instanceof ProcedureCallContext procedureCallContext) {
                String qualifiedProcedureName = procedureCallContext.procedureName.getQualifiedName();
                ExposesCall.ExposesYield<StatementBuilder.OngoingInQueryCallWithReturnFields> exposesYield;
                StatementBuilder.OngoingInQueryCallWithoutArguments ongoingInQueryCallWithoutArguments = orderableOngoingReadingAndWithWithoutWhere.call(qualifiedProcedureName);
                exposesYield = ongoingInQueryCallWithoutArguments;
                if (procedureCallContext.arguments.size() > 0) {
                    StatementBuilder.OngoingInQueryCallWithArguments ongoingInQueryCallWithArguments = ongoingInQueryCallWithoutArguments
                            .withArgs(procedureCallContext.arguments.toArray(new Expression[0]));
                    exposesYield = ongoingInQueryCallWithArguments;
                }
                AliasedExpression[] aliasedExpressionsArray = new AliasedExpression[procedureCallContext.yieldExpressions.size()];
                SymbolicName[] symbolicNamesArray = new SymbolicName[procedureCallContext.yieldExpressions.size()];
                boolean hasAliasedExpression = false;
                boolean hasSymbolicName = false;
                for (int i = 0; i < procedureCallContext.yieldExpressions.size(); i++) {
                    Expression yieldExpression = procedureCallContext.yieldExpressions.get(i);
                    if (yieldExpression instanceof AliasedExpression aliasedExpression) {
                        aliasedExpressionsArray[i] = aliasedExpression;
                        hasAliasedExpression = true;
                    } else if (yieldExpression instanceof SymbolicName symbolicName) {
                        symbolicNamesArray[i] = symbolicName;
                        hasSymbolicName = true;
                    } else {
                        throw new CypherDslQueryTransformerException("Unsupported yield expression type: " + yieldExpression.getClass());
                    }
                }
                if (hasAliasedExpression) {
                    exposesReturning = exposesYield.yield(aliasedExpressionsArray);
                    if (hasSymbolicName) {
                        throw new CypherDslQueryTransformerException("ProcedureCall contains arguments of both AliasedExpression and SymbolicName");
                    }
                } else if (hasSymbolicName) {
                    exposesReturning = exposesYield.yield(symbolicNamesArray);
                }
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
        StatementBuilder.OngoingReadingAndReturn ongoingReadingAndReturn = exposesReturning.returning(statementContext.returnContext.expressions);
        ResultStatement resultStatement = ongoingReadingAndReturn.build();
        transformAndPushUp(resultStatement);
    }

    void enter(Subquery subquery) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), subquery.getClass().getSimpleName());
        }
        operatorStack.push(new SubqueryContext());
    }

    void leave(Subquery subquery) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), subquery.getClass().getSimpleName());
        }
        SubqueryContext subqueryContext = (SubqueryContext) operatorStack.pop();
        transformAndPushUp(subqueryContext);
    }

    void enter(With with) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), with.getClass().getSimpleName());
        }
        operatorStack.push(new WithContext());
    }

    void leave(With with) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), with.getClass().getSimpleName());
        }
        WithContext withContext = (WithContext) operatorStack.pop();
        transformAndPushUp(withContext);
    }

    void enter(Return returning) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), returning.getClass().getSimpleName());
        }
        operatorStack.push(new ReturnContext());
    }

    void leave(Return returning) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), returning.getClass().getSimpleName());
        }
        ReturnContext returnContext = (ReturnContext) operatorStack.pop();
        transformAndPushUp(returnContext);
    }

    void enter(Expression expression) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), expression.getClass().getSimpleName());
        }
        operatorStack.push(new Object());
    }

    void leave(Expression expression) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), expression.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(expression);
    }

    void enter(Literal literal) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), literal.getClass().getSimpleName());
        }
    }

    void leave(Literal literal) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), literal.getClass().getSimpleName());
        }
        transformAndPushUp(literal);
    }

    void enter(Match match) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), match.getClass().getSimpleName());
        }
        operatorStack.push(new MatchContext(match, match.isOptional()));
    }

    void leave(Match match) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), match.getClass().getSimpleName());
        }
        MatchContext matchContext = (MatchContext) operatorStack.pop();
        transformAndPushUp(matchContext);
    }

    interface TransformPredicate<T> {
        boolean shouldTransform(Deque<Object> operatorStack, T t);
    }

    @FunctionalInterface
    interface TransformFunction<CONTEXT> {
        CONTEXT apply(Deque<Object> operatorStack, CONTEXT input);
    }

    protected static class TransformPredicateAndFunction<T> {
        final TransformPredicate<T> predicate;
        final TransformFunction<T> function;

        TransformPredicateAndFunction(TransformPredicate<T> predicate, TransformFunction<T> function) {
            this.predicate = predicate;
            this.function = function;
        }
    }

    final Map<Class<?>, List<TransformPredicateAndFunction<?>>> transformers = new LinkedHashMap<>();

    public <T> GenericCypherDslQueryTransformer registerTransform(Class<T> clazz, TransformPredicate<T> predicate, TransformFunction<T> transformFunction) {
        List<TransformPredicateAndFunction<?>> transformPredicateAndFunctions = transformers.computeIfAbsent(clazz, key -> new LinkedList<>());
        transformPredicateAndFunctions.add(new TransformPredicateAndFunction<>(predicate, transformFunction));
        return this;
    }

    private <T> T doTransform(T ctx) {
        Class<T> clazz = (Class<T>) ctx.getClass();
        List<TransformPredicateAndFunction<?>> transformPredicateAndFunctions = transformers.get(clazz);
        if (transformPredicateAndFunctions == null) {
            return ctx;
        }
        for (TransformPredicateAndFunction<?> transformPredicateAndFunction : transformPredicateAndFunctions) {
            TransformPredicateAndFunction<T> typedTransformPredicateAndFunction = (TransformPredicateAndFunction<T>) transformPredicateAndFunction;
            if (typedTransformPredicateAndFunction.predicate.shouldTransform(operatorStack, ctx)) {
                T output = typedTransformPredicateAndFunction.function.apply(operatorStack, ctx);
                return output;
            }
        }
        return ctx;
    }

    void enter(PatternElement patternElement) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), patternElement.getClass().getSimpleName());
        }
        operatorStack.push(new Object());
    }

    void leave(PatternElement patternElement) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), patternElement.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(patternElement);
    }

    void enter(Node node) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), node.getClass().getSimpleName());
        }
        operatorStack.push(new Object());
    }

    void leave(Node node) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), node.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(node);
    }

    void enter(RelationshipPattern relationshipPattern) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), relationshipPattern.getClass().getSimpleName());
        }
        operatorStack.push(new Object());
    }

    void leave(RelationshipPattern relationshipPattern) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), relationshipPattern.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(relationshipPattern);
    }

    void enter(ProcedureCall procedureCall) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), procedureCall.getClass().getSimpleName());
        }
        operatorStack.push(new ProcedureCallContext().add(procedureCall));
    }

    void leave(ProcedureCall procedureCall) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), procedureCall.getClass().getSimpleName());
        }
        ProcedureCallContext procedureCallContext = (ProcedureCallContext) operatorStack.pop();
        transformAndPushUp(procedureCallContext);
    }

    void enter(ProcedureName procedureName) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), procedureName.getClass().getSimpleName());
        }
        operatorStack.push(new Object()); // protect procedure-arguments from seeing procedure namespace as the first argument
    }

    void leave(ProcedureName procedureName) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), procedureName.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(procedureName);
    }

    void enter(YieldItems yieldItems) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), yieldItems.getClass().getSimpleName());
        }
        operatorStack.push(new Object()); // protect procedureCallContext from interpreting this as an argument
    }

    void leave(YieldItems yieldItems) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), yieldItems.getClass().getSimpleName());
        }
        Object yieldItemsContext = (Object) operatorStack.pop();
    }

    void enter(Where where) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), where.getClass().getSimpleName());
        }
        operatorStack.push(new WhereContext());
    }

    void leave(Where where) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), where.getClass().getSimpleName());
        }
        WhereContext whereContext = (WhereContext) operatorStack.pop();
        transformAndPushUp(whereContext.condition);
    }

    void enter(Condition condition) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), condition.getClass().getSimpleName());
        }
        operatorStack.push(new ConditionContext());
    }

    void leave(Condition condition) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), condition.getClass().getSimpleName());
        }
        ConditionContext conditionContext = (ConditionContext) operatorStack.pop();
        transformAndPushUp(condition);
    }

    static class RelationshipContext {
    }

    void enter(Relationship relationship) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), relationship.getClass().getSimpleName());
        }
        operatorStack.push(new RelationshipContext());
    }

    void leave(Relationship relationship) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), relationship.getClass().getSimpleName());
        }
        RelationshipContext relationshipContext = (RelationshipContext) operatorStack.pop();
        transformAndPushUp(relationship);
    }
}
