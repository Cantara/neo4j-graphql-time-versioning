package com.exoreaction.xorcery.tbv.neo4j.opencypherdsl;

import org.neo4j.cypherdsl.core.AliasedExpression;
import org.neo4j.cypherdsl.core.Condition;
import org.neo4j.cypherdsl.core.ExposesCall;
import org.neo4j.cypherdsl.core.ExposesMatch;
import org.neo4j.cypherdsl.core.ExposesReturning;
import org.neo4j.cypherdsl.core.ExposesSubqueryCall;
import org.neo4j.cypherdsl.core.ExposesWhere;
import org.neo4j.cypherdsl.core.Expression;
import org.neo4j.cypherdsl.core.Limit;
import org.neo4j.cypherdsl.core.Literal;
import org.neo4j.cypherdsl.core.Match;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.NumberLiteral;
import org.neo4j.cypherdsl.core.Order;
import org.neo4j.cypherdsl.core.PatternElement;
import org.neo4j.cypherdsl.core.ProcedureCall;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.Relationship;
import org.neo4j.cypherdsl.core.RelationshipPattern;
import org.neo4j.cypherdsl.core.ResultStatement;
import org.neo4j.cypherdsl.core.Return;
import org.neo4j.cypherdsl.core.SortItem;
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

    protected static class StatementContext implements SubqueryOperandAware, WithOperandAware, MatchOperandAware, ReturnOperandAware {

        protected final Statement originalStatement;

        protected final List<Object> clauses = new LinkedList<>();
        protected final List<SubqueryContext> subqueryContexts = new LinkedList<>();
        protected final List<WithContext> withContexts = new LinkedList<>();
        protected final List<MatchContext> matchContexts = new LinkedList<>();
        protected ReturnContext returnContext;

        public StatementContext(Statement originalStatement) {
            this.originalStatement = originalStatement;
        }

        public Statement originalStatement() {
            return originalStatement;
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

    protected static class MatchContext implements PatternElementOperandAware, WhereOperandAware {
        protected final Match originalMatch;
        protected final boolean isOptional;

        protected final List<PatternElement> patternElements = new LinkedList<>();

        protected Condition condition;

        protected MatchContext(Match originalMatch, boolean isOptional) {
            this.originalMatch = originalMatch;
            this.isOptional = isOptional;
        }

        public Match originalMatch() {
            return originalMatch;
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

    protected static class SubqueryContext implements StatementOperandAware {
        protected Statement statement;

        @Override
        public StatementOperandAware add(Object operand) {
            if (this.statement != null) {
                throw new CypherDslQueryTransformerException();
            }
            this.statement = (Statement) operand;
            return this;
        }
    }

    protected static class ProcedureCallContext implements StatementOperandAware {
        protected ProcedureName procedureName;
        protected Statement statement;
        protected final List<Expression> arguments = new ArrayList<>();
        protected final List<Expression> yieldExpressions = new ArrayList<>();

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

    protected static class SortItemContext implements ExpressionOperandAware, SortItemDirectionOperandAware {

        Expression expression;
        SortItem.Direction direction;

        private SortItemContext withDirection(SortItem.Direction direction) {
            this.direction = direction;
            return this;
        }

        @Override
        public SortItemContext add(Object operand) {
            if (operand instanceof Expression expression) {
                this.expression = expression;
            } else if (operand instanceof SortItem.Direction direction) {
                this.direction = direction;
            }
            return null;
        }
    }

    protected static class OrderContext implements SortItemOperandAware {

        final List<SortItem> sortItems = new ArrayList<>();

        @Override
        public SortItemContext add(Object operand) {
            if (operand instanceof SortItem sortItem) {
                this.sortItems.add(sortItem);
            }
            return null;
        }
    }

    protected static class WithContext implements ExpressionOperandAware, OrderOperandAware {

        private final List<Expression> expressions = new LinkedList<>();
        private OrderContext orderContext;

        @Override
        public WithContext add(Object operand) {
            if (operand instanceof OrderContext orderContext) {
                this.orderContext = orderContext;
            } else if (operand instanceof Expression expression) {
                this.expressions.add(expression);
            }
            return this;
        }
    }

    protected interface OperandAware {
        OperandAware add(Object operand);
    }

    protected interface SubqueryOperandAware extends OperandAware {
    }

    protected interface ConditionOperandAware extends OperandAware {
    }

    protected interface MatchOperandAware extends OperandAware {
    }

    protected interface WhereOperandAware extends OperandAware {
    }

    protected interface PatternElementOperandAware extends OperandAware {
    }

    protected interface ReturnOperandAware extends OperandAware {
    }

    protected interface StatementOperandAware extends OperandAware {
    }

    protected interface WithOperandAware extends OperandAware {
    }

    protected interface ExpressionOperandAware extends OperandAware {
    }

    protected interface OrderOperandAware extends OperandAware {
    }

    protected interface SortItemOperandAware extends OperandAware {
    }

    protected interface SortItemDirectionOperandAware extends OperandAware {
    }

    protected static class WhereContext implements ConditionOperandAware {
        protected Condition condition;

        @Override
        public WhereContext add(Object operand) {
            this.condition = (Condition) operand;
            return this;
        }
    }

    protected static class ConditionContext {
    }

    protected static class ReturnContext implements ExpressionOperandAware {
        protected final List<Expression> expressions = new LinkedList<>();
        protected LimitContext limitContext;

        @Override
        public ReturnContext add(Object operand) {
            if (operand instanceof Expression expression) {
                expressions.add(expression);
            } else if (operand instanceof LimitContext limitContext) {
                this.limitContext = limitContext;
            }
            return this;
        }
    }

    private final Deque<Object> operatorStack = new LinkedList<>();

    protected void transformAndPushUp(Object operand) {
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

    protected void enter(Statement statement) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), statement.getClass().getSimpleName());
        }
        operatorStack.push(new StatementContext(statement));
    }

    protected void leave(Statement statement) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), statement.getClass().getSimpleName());
        }
        StatementContext statementContext = (StatementContext) operatorStack.pop();
        StatementBuilder statementBuilder = Statement.builder();
        ExposesMatch exposesMatch = statementBuilder;
        StatementBuilder.ExposesWith exposesWith = null;
        ExposesSubqueryCall exposesSubqueryCall = statementBuilder;
        ExposesReturning exposesReturning = statementBuilder;
        ExposesWhere exposesWhere = null;
        StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere orderableOngoingReadingAndWithWithoutWhere = null;
        StatementBuilder.OrderableOngoingReadingAndWithWithWhere orderableOngoingReadingAndWithWithWhere = null;
        for (Object clause : statementContext.clauses) {
            if (clause instanceof WithContext withContext) {
                if (exposesWith == null) {
                    orderableOngoingReadingAndWithWithoutWhere = statementBuilder.with(withContext.expressions);
                    exposesWith = orderableOngoingReadingAndWithWithoutWhere;
                    exposesMatch = orderableOngoingReadingAndWithWithoutWhere;
                    exposesSubqueryCall = orderableOngoingReadingAndWithWithoutWhere;
                    exposesReturning = orderableOngoingReadingAndWithWithoutWhere;
                    if (withContext.orderContext != null) {
                        orderableOngoingReadingAndWithWithWhere = orderableOngoingReadingAndWithWithoutWhere.orderBy(withContext.orderContext.sortItems);
                        exposesWith = orderableOngoingReadingAndWithWithWhere;
                        exposesMatch = orderableOngoingReadingAndWithWithWhere;
                        exposesSubqueryCall = orderableOngoingReadingAndWithWithWhere;
                        exposesReturning = orderableOngoingReadingAndWithWithWhere;
                    }
                } else {
                    orderableOngoingReadingAndWithWithoutWhere = exposesWith.with(withContext.expressions);
                    exposesWith = orderableOngoingReadingAndWithWithoutWhere;
                    exposesMatch = orderableOngoingReadingAndWithWithoutWhere;
                    exposesSubqueryCall = orderableOngoingReadingAndWithWithoutWhere;
                    exposesReturning = orderableOngoingReadingAndWithWithoutWhere;
                    if (withContext.orderContext != null) {
                        orderableOngoingReadingAndWithWithWhere = orderableOngoingReadingAndWithWithoutWhere.orderBy(withContext.orderContext.sortItems);
                        exposesWith = orderableOngoingReadingAndWithWithWhere;
                        exposesMatch = orderableOngoingReadingAndWithWithWhere;
                        exposesSubqueryCall = orderableOngoingReadingAndWithWithWhere;
                        exposesReturning = orderableOngoingReadingAndWithWithWhere;
                    }
                }
            } else if (clause instanceof SubqueryContext subqueryContext) {
                StatementBuilder.OngoingReadingWithoutWhere ongoingReadingWithoutWhere = exposesSubqueryCall.call(subqueryContext.statement);
                exposesWith = ongoingReadingWithoutWhere;
                exposesMatch = ongoingReadingWithoutWhere;
                exposesSubqueryCall = ongoingReadingWithoutWhere;
                exposesReturning = ongoingReadingWithoutWhere;
                exposesWhere = ongoingReadingWithoutWhere;
            } else if (clause instanceof ProcedureCallContext procedureCallContext) {
                String qualifiedProcedureName = procedureCallContext.procedureName.getQualifiedName();
                ExposesCall.ExposesYield<StatementBuilder.OngoingInQueryCallWithReturnFields> exposesYield;
                StatementBuilder.OngoingInQueryCallWithoutArguments ongoingInQueryCallWithoutArguments;
                if (orderableOngoingReadingAndWithWithWhere != null) {
                    ongoingInQueryCallWithoutArguments = orderableOngoingReadingAndWithWithWhere.call(qualifiedProcedureName);
                } else {
                    ongoingInQueryCallWithoutArguments = orderableOngoingReadingAndWithWithoutWhere.call(qualifiedProcedureName);
                }
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
                    StatementBuilder.OngoingInQueryCallWithReturnFields yield = exposesYield.yield(aliasedExpressionsArray);
                    exposesMatch = yield;
                    exposesWhere = yield;
                    exposesReturning = yield;
                    exposesWith = yield;
                    exposesSubqueryCall = yield;
                    if (hasSymbolicName) {
                        throw new CypherDslQueryTransformerException("ProcedureCall contains arguments of both AliasedExpression and SymbolicName");
                    }
                } else if (hasSymbolicName) {
                    StatementBuilder.OngoingInQueryCallWithReturnFields yield = exposesYield.yield(symbolicNamesArray);
                    exposesMatch = yield;
                    exposesWhere = yield;
                    exposesReturning = yield;
                    exposesWith = yield;
                    exposesSubqueryCall = yield;
                } else {
                    throw new CypherDslQueryTransformerException("Missing YIELD statement after procedure-call");
                }
            } else if (clause instanceof MatchContext matchContext) {
                StatementBuilder.OngoingReadingWithoutWhere ongoingReadingWithoutWhere = exposesMatch.match(matchContext.isOptional, matchContext.patternElements.toArray(new PatternElement[0]));
                exposesWith = ongoingReadingWithoutWhere;
                exposesMatch = ongoingReadingWithoutWhere;
                exposesSubqueryCall = ongoingReadingWithoutWhere;
                exposesReturning = ongoingReadingWithoutWhere;
                exposesWhere = ongoingReadingWithoutWhere;
                if (matchContext.condition != null) {
                    StatementBuilder.OngoingReadingWithWhere ongoingReadingWithWhere = ongoingReadingWithoutWhere.where(matchContext.condition);
                    exposesWith = ongoingReadingWithWhere;
                    exposesMatch = ongoingReadingWithWhere;
                    exposesSubqueryCall = ongoingReadingWithWhere;
                    exposesReturning = ongoingReadingWithWhere;
                    exposesWhere = ongoingReadingWithoutWhere;
                }
            } else if (clause instanceof ReturnContext returnContext) {
                // handled at end after clauses loop
            }
        }
        if (statementContext.returnContext == null) {
            throw new CypherDslQueryTransformerException();
        }
        StatementBuilder.OngoingReadingAndReturn ongoingReadingAndReturn = exposesReturning.returning(statementContext.returnContext.expressions);
        StatementBuilder.BuildableStatement<ResultStatement> buildableStatementWithResult = ongoingReadingAndReturn;
        if (statementContext.returnContext.limitContext != null) {
            if (statementContext.returnContext.limitContext.numberLiteral != null) {
                buildableStatementWithResult = ongoingReadingAndReturn.limit(statementContext.returnContext.limitContext.numberLiteral);
            } else if (statementContext.returnContext.limitContext.expression != null) {
                buildableStatementWithResult = ongoingReadingAndReturn.limit(statementContext.returnContext.limitContext.expression);
            } else {
                throw new CypherDslQueryTransformerException();
            }
        }
        ResultStatement resultStatement = buildableStatementWithResult.build();
        transformAndPushUp(resultStatement);
    }

    protected void enter(Subquery subquery) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), subquery.getClass().getSimpleName());
        }
        operatorStack.push(new SubqueryContext());
    }

    protected void leave(Subquery subquery) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), subquery.getClass().getSimpleName());
        }
        SubqueryContext subqueryContext = (SubqueryContext) operatorStack.pop();
        transformAndPushUp(subqueryContext);
    }

    protected void enter(With with) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), with.getClass().getSimpleName());
        }
        operatorStack.push(new WithContext());
    }

    protected void leave(With with) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), with.getClass().getSimpleName());
        }
        WithContext withContext = (WithContext) operatorStack.pop();
        transformAndPushUp(withContext);
    }

    protected void enter(Return returning) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), returning.getClass().getSimpleName());
        }
        operatorStack.push(new ReturnContext());
    }

    protected void leave(Return returning) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), returning.getClass().getSimpleName());
        }
        ReturnContext returnContext = (ReturnContext) operatorStack.pop();
        transformAndPushUp(returnContext);
    }

    protected class LimitContext implements OperandAware {
        protected NumberLiteral numberLiteral;
        protected Expression expression;

        @Override
        public OperandAware add(Object operand) {
            if (operand instanceof NumberLiteral numberLiteral) {
                this.numberLiteral = numberLiteral;
            } else if (operand instanceof Expression expression) {
                this.expression = expression;
            } else {
                throw new CypherDslQueryTransformerException("Expected NumberLiteral or Expression limit");
            }
            return null;
        }
    }

    protected void enter(Limit limit) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), limit.getClass().getSimpleName());
        }
        operatorStack.push(new LimitContext());
    }

    protected void leave(Limit limit) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), limit.getClass().getSimpleName());
        }
        LimitContext limitContext = (LimitContext) operatorStack.pop();
        transformAndPushUp(limitContext);
    }

    protected void enter(NumberLiteral numberLiteral) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), numberLiteral.getClass().getSimpleName());
        }
    }

    protected void leave(NumberLiteral numberLiteral) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), numberLiteral.getClass().getSimpleName());
        }
        transformAndPushUp(numberLiteral);
    }

    protected void enter(Expression expression) {
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

    protected void enter(Property property) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), property.getClass().getSimpleName());
        }
        operatorStack.push(new Object());
    }

    void leave(Property property) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), property.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(property);
    }

    protected void enter(Literal literal) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), literal.getClass().getSimpleName());
        }
    }

    protected void leave(Literal literal) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), literal.getClass().getSimpleName());
        }
        transformAndPushUp(literal);
    }

    protected void enter(Match match) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), match.getClass().getSimpleName());
        }
        operatorStack.push(new MatchContext(match, match.isOptional()));
    }

    protected void leave(Match match) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), match.getClass().getSimpleName());
        }
        MatchContext matchContext = (MatchContext) operatorStack.pop();
        transformAndPushUp(matchContext);
    }

    protected void enter(Order order) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), order.getClass().getSimpleName());
        }
        operatorStack.push(new OrderContext());
    }

    protected void leave(Order order) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), order.getClass().getSimpleName());
        }
        OrderContext orderContext = (OrderContext) operatorStack.pop();
        transformAndPushUp(orderContext);
    }

    protected void enter(SortItem sortItem) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), sortItem.getClass().getSimpleName());
        }
        operatorStack.push(new SortItemContext());
    }

    protected void leave(SortItem sortItem) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), sortItem.getClass().getSimpleName());
        }
        SortItemContext sortItemContext = (SortItemContext) operatorStack.pop();
        // TODO for now context is discarded -- no way to transform context back to SortItem instance
        transformAndPushUp(sortItem);
    }

    protected void enter(SortItem.Direction direction) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), direction.getClass().getSimpleName());
        }
    }

    protected void leave(SortItem.Direction direction) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), direction.getClass().getSimpleName());
        }
        transformAndPushUp(direction);
    }

    protected interface TransformPredicate<T> {
        boolean shouldTransform(Deque<Object> operatorStack, T t);
    }

    @FunctionalInterface
    protected interface TransformFunction<CONTEXT> {
        CONTEXT apply(Deque<Object> operatorStack, CONTEXT input);
    }

    protected static class TransformPredicateAndFunction<T> {
        protected final TransformPredicate<T> predicate;
        protected final TransformFunction<T> function;

        protected TransformPredicateAndFunction(TransformPredicate<T> predicate, TransformFunction<T> function) {
            this.predicate = predicate;
            this.function = function;
        }
    }

    protected final Map<Class<?>, List<TransformPredicateAndFunction<?>>> transformers = new LinkedHashMap<>();

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

    protected void enter(PatternElement patternElement) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), patternElement.getClass().getSimpleName());
        }
        operatorStack.push(new Object());
    }

    protected void leave(PatternElement patternElement) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), patternElement.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(patternElement);
    }

    protected void enter(Node node) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), node.getClass().getSimpleName());
        }
        operatorStack.push(new Object());
    }

    protected void leave(Node node) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), node.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(node);
    }

    protected void enter(RelationshipPattern relationshipPattern) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), relationshipPattern.getClass().getSimpleName());
        }
        operatorStack.push(new Object());
    }

    protected void leave(RelationshipPattern relationshipPattern) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), relationshipPattern.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(relationshipPattern);
    }

    protected void enter(ProcedureCall procedureCall) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), procedureCall.getClass().getSimpleName());
        }
        operatorStack.push(new ProcedureCallContext().add(procedureCall));
    }

    protected void leave(ProcedureCall procedureCall) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), procedureCall.getClass().getSimpleName());
        }
        ProcedureCallContext procedureCallContext = (ProcedureCallContext) operatorStack.pop();
        transformAndPushUp(procedureCallContext);
    }

    protected void enter(ProcedureName procedureName) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), procedureName.getClass().getSimpleName());
        }
        operatorStack.push(new Object()); // protect procedure-arguments from seeing procedure namespace as the first argument
    }

    protected void leave(ProcedureName procedureName) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), procedureName.getClass().getSimpleName());
        }
        operatorStack.pop();
        transformAndPushUp(procedureName);
    }

    protected void enter(YieldItems yieldItems) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), yieldItems.getClass().getSimpleName());
        }
        operatorStack.push(new Object()); // protect procedureCallContext from interpreting this as an argument
    }

    protected void leave(YieldItems yieldItems) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), yieldItems.getClass().getSimpleName());
        }
        Object yieldItemsContext = (Object) operatorStack.pop();
    }

    protected void enter(Where where) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), where.getClass().getSimpleName());
        }
        operatorStack.push(new WhereContext());
    }

    protected void leave(Where where) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), where.getClass().getSimpleName());
        }
        WhereContext whereContext = (WhereContext) operatorStack.pop();
        transformAndPushUp(whereContext.condition);
    }

    protected void enter(Condition condition) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), condition.getClass().getSimpleName());
        }
        operatorStack.push(new ConditionContext());
    }

    protected void leave(Condition condition) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), condition.getClass().getSimpleName());
        }
        ConditionContext conditionContext = (ConditionContext) operatorStack.pop();
        transformAndPushUp(condition);
    }

    protected static class RelationshipContext {
    }

    protected void enter(Relationship relationship) {
        if (debug) {
            System.out.printf("%sENTER %s%n", "| ".repeat(depth), relationship.getClass().getSimpleName());
        }
        operatorStack.push(new RelationshipContext());
    }

    protected void leave(Relationship relationship) {
        if (debug) {
            System.out.printf("%sLEAVE %s%n", "| ".repeat(depth), relationship.getClass().getSimpleName());
        }
        RelationshipContext relationshipContext = (RelationshipContext) operatorStack.pop();
        transformAndPushUp(relationship);
    }
}
