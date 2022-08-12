package com.exoreaction.xorcery.tbv.graphql.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import static graphql.introspection.Introspection.DirectiveLocation;

public class TBVDirectives {

    public static final String SEARCHABLE_NAME = "searchable";
    public static final String PAGINATION_NAME = "pagination";
    public static final String REVERSE_NAME_NAME = "reverseName";
    public static final String MAPPED_BY_NAME = "mappedBy";

    private static final GraphQLArgument SEARCHABLE_ARGUMENT = GraphQLArgument.newArgument()
            .name(SEARCHABLE_NAME)
            .type(GraphQLBoolean)
            .defaultValue(true)
            .build();

    private static final GraphQLArgument PAGINATION_ARGUMENT = GraphQLArgument.newArgument()
            .name(PAGINATION_NAME)
            .type(GraphQLBoolean)
            .defaultValue(true)
            .build();

    private static final GraphQLArgument REVERSE_NAME_ARGUMENT = GraphQLArgument.newArgument()
            .name(REVERSE_NAME_NAME)
            .type(GraphQLString)
            .build();

    private static final GraphQLArgument MAPPED_BY_ARGUMENT = GraphQLArgument.newArgument()
            .name(MAPPED_BY_NAME)
            .type(GraphQLString)
            .build();

    public static GraphQLDirective DOMAIN = GraphQLDirective.newDirective()
            .name("domain")
            .description("Marks a type definition as a managed domain object")
            .validLocations(DirectiveLocation.OBJECT)
            .argument(SEARCHABLE_ARGUMENT)
            .build();

    public static GraphQLDirective LINK = GraphQLDirective.newDirective()
            .name("link")
            .description("Defines a field as a link to another managed domain")
            .validLocations(DirectiveLocation.FIELD_DEFINITION)
            .argument(PAGINATION_ARGUMENT)
            .argument(REVERSE_NAME_ARGUMENT)
            .build();

    public static GraphQLDirective REVERSE_LINK = GraphQLDirective.newDirective()
            .name("reverseLink")
            .description("Defines a field as a reverse link to another managed domain")
            .validLocations(DirectiveLocation.FIELD_DEFINITION)
            .argument(PAGINATION_ARGUMENT)
            .argument(MAPPED_BY_ARGUMENT)
            .build();
}
