package com.exoreaction.xorcery.tbv.graphql.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;

import java.util.EnumSet;
import java.util.List;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.introspection.Introspection.DirectiveLocation;

public class DomainDirective extends GraphQLDirective {

    public static final String NAME = "domain";
    private static final String SEARCHABLE_NAME = "searchable";
    private static final GraphQLArgument SEARCHABLE_ARGUMENT = GraphQLArgument.newArgument()
            .name(SEARCHABLE_NAME)
            .type(GraphQLBoolean)
            .defaultValue(true)
            .build();
    private static final String DESCRIPTION = "Marks a type definition as a managed domain object";

    public static DomainDirective INSTANCE = new DomainDirective(
            NAME,
            DESCRIPTION,
            EnumSet.of(DirectiveLocation.OBJECT),
            List.of(SEARCHABLE_ARGUMENT)
    );

    private DomainDirective(String name, String description, EnumSet<DirectiveLocation> locations, List<GraphQLArgument> arguments) {
        super(name, description, locations, arguments);
    }

    public static DomainDirective newDomainDirective(Boolean searchable) {
        return new DomainDirective(
                NAME,
                DESCRIPTION,
                EnumSet.of(DirectiveLocation.OBJECT),
                List.of(SEARCHABLE_ARGUMENT.transform(builder -> builder.value(searchable)))
        );
    }

    public static Boolean hasDomainDirective(GraphQLDirectiveContainer container) {
        for (GraphQLDirective directive : container.getDirectives()) {
            if (directive.getName().equals(DomainDirective.NAME)) {
                return true;
            }
        }
        return false;
    }

    public Boolean isSearchable() {
        GraphQLArgument argument = getArgument(SEARCHABLE_NAME);
        Object value = argument.getValue();
        if (value != null) {
            return (Boolean) value;
        }
        return (Boolean) argument.getDefaultValue();
    }

}
