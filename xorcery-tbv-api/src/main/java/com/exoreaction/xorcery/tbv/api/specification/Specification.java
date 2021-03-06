package com.exoreaction.xorcery.tbv.api.specification;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.util.Set;

public interface Specification {

    SpecificationElement getRootElement();

    Set<String> getManagedDomains();

    default TypeDefinitionRegistry typeDefinitionRegistry() {
        throw new UnsupportedOperationException();
    }

    default GraphQLSchema schema() {
        throw new UnsupportedOperationException();
    }
}
