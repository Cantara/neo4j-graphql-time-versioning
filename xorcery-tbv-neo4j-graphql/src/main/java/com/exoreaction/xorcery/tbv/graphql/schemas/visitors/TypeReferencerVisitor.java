package com.exoreaction.xorcery.tbv.graphql.schemas.visitors;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedSchemaElement;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.schema.GraphQLUnionType;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Given a typeMap this visitor will replace all types with type references.
 */
public class TypeReferencerVisitor extends GraphQLTypeVisitorStub {

    private static final Logger log = LoggerFactory.getLogger(TypeReferencerVisitor.class);
    private final Map<String, GraphQLNamedType> typeMap;

    public TypeReferencerVisitor(Map<String, GraphQLNamedType> typeMap) {
        this.typeMap = Objects.requireNonNull(typeMap);
    }

    @Override
    public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context) {
        GraphQLInterfaceType.Builder newInterfaceBuilder = GraphQLInterfaceType.newInterface(
                (GraphQLInterfaceType) typeMap.get(node.getName()));
        for (GraphQLFieldDefinition fieldDefinition : node.getFieldDefinitions()) {
            AtomicBoolean changes = new AtomicBoolean(false);
            GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition(fieldDefinition);
            convertToReference(fieldDefinition.getType()).ifPresent(reference -> {
                fieldBuilder.type((GraphQLOutputType) reference);
                changes.set(true);
            });
            for (GraphQLArgument argument : fieldDefinition.getArguments()) {
                convertToReference(argument).ifPresent(reference -> {
                    fieldBuilder.argument((GraphQLArgument) reference);
                    changes.set(true);
                });
            }
            if (changes.get()) {
                newInterfaceBuilder.field(fieldBuilder.build());
            }
        }

        GraphQLInterfaceType newInterface = newInterfaceBuilder.build();
        GraphQLType oldObject = typeMap.put(node.getName(), newInterface);
        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context) {
        GraphQLUnionType.Builder newUnionTypeBuilder = GraphQLUnionType.newUnionType(
                (GraphQLUnionType) typeMap.get(node.getName()));
        for (GraphQLOutputType type : node.getTypes()) {
            if (!(type instanceof GraphQLTypeReference)) {
                newUnionTypeBuilder.possibleType(GraphQLTypeReference.typeRef(((GraphQLNamedSchemaElement) type).getName()));
            }
        }
        GraphQLUnionType newUnionType = newUnionTypeBuilder.build();
        GraphQLType oldObject = typeMap.put(node.getName(), newUnionType);
        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context) {
        GraphQLObjectType.Builder newObjectBuilder = GraphQLObjectType.newObject(
                (GraphQLObjectType) typeMap.get(node.getName()));
        for (GraphQLFieldDefinition fieldDefinition : node.getFieldDefinitions()) {
            AtomicBoolean changes = new AtomicBoolean(false);
            GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition(fieldDefinition);
            convertToReference(fieldDefinition.getType()).ifPresent(reference -> {
                fieldBuilder.type((GraphQLOutputType) reference);
                changes.set(true);
            });
            for (GraphQLArgument argument : fieldDefinition.getArguments()) {
                convertToReference(argument).ifPresent(reference -> {
                    fieldBuilder.argument((GraphQLArgument) reference);
                    changes.set(true);
                });
            }
            if (changes.get()) {
                newObjectBuilder.field(fieldBuilder.build());
            }
        }
        for (GraphQLOutputType anInterface : node.getInterfaces()) {
            convertToReference(anInterface).ifPresent(reference -> {
                newObjectBuilder.withInterfaces(GraphQLTypeReference.typeRef(((GraphQLNamedSchemaElement) reference).getName()));
            });
        }
        GraphQLObjectType newObject = newObjectBuilder.build();
        GraphQLType oldObject = typeMap.put(node.getName(), newObject);
        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLInputObjectType(GraphQLInputObjectType node, TraverserContext<GraphQLSchemaElement> context) {
        GraphQLInputObjectType.Builder newInputObjectBuilder = GraphQLInputObjectType.newInputObject(
                (GraphQLInputObjectType) typeMap.get(node.getName()));
        for (GraphQLInputObjectField fieldDefinition : node.getFieldDefinitions()) {
            convertToReference(fieldDefinition.getType()).ifPresent(reference -> {
                newInputObjectBuilder.field(GraphQLInputObjectField.newInputObjectField(fieldDefinition).type((GraphQLInputType) reference).build());
            });
        }
        GraphQLInputObjectType newObject = newInputObjectBuilder.build();
        GraphQLType oldObject = typeMap.put(node.getName(), newObject);
        return TraversalControl.CONTINUE;
    }

    private Optional<GraphQLSchemaElement> convertToReference(GraphQLSchemaElement arg) {
        GraphQLArgument graphQLArgument = null;
        GraphQLType type;
        if (arg instanceof GraphQLArgument) {
            graphQLArgument = (GraphQLArgument) arg;
            type = graphQLArgument.getType();
        } else if (arg instanceof GraphQLType) {
            type = (GraphQLType) arg;
        } else {
            throw new IllegalArgumentException("arg must be of either GraphQLArgument or GraphQLType");
        }
        Stack<GraphQLType> types = GraphQLTypeUtil.unwrapType(type);
        GraphQLType current = types.pop();
        if (current instanceof GraphQLTypeReference || current instanceof GraphQLScalarType) {
            return Optional.empty();
        }
        if (!typeMap.containsKey(((GraphQLNamedSchemaElement) current).getName())) {
            throw new AssertionError("type was not in type map");
        }
        GraphQLType newType = GraphQLTypeReference.typeRef(((GraphQLNamedSchemaElement) current).getName());
        while (!types.empty()) {
            current = types.pop();
            if (GraphQLTypeUtil.isList(current)) {
                newType = GraphQLList.list(newType);
            } else if (GraphQLTypeUtil.isNonNull(current)) {
                newType = GraphQLNonNull.nonNull(newType);
            } else {
                throw new AssertionError("non wrapped type up the stack");
            }
        }
        if (graphQLArgument != null) {
            return Optional.of(GraphQLArgument.newArgument(graphQLArgument).type((GraphQLInputType) newType).build());
        }
        return Optional.of(newType);
    }
}
