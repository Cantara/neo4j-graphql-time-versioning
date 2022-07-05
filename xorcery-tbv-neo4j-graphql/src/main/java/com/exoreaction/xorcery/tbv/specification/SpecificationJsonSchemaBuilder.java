package com.exoreaction.xorcery.tbv.specification;

import com.exoreaction.xorcery.tbv.api.specification.SpecificationElement;
import com.exoreaction.xorcery.tbv.api.specification.SpecificationElementType;
import com.exoreaction.xorcery.tbv.schema.JsonSchema;
import com.exoreaction.xorcery.tbv.schema.JsonSchemaDefinitionElement;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.util.LinkedHashSet;

public class SpecificationJsonSchemaBuilder {

    public static SpecificationJsonSchemaBuilder createBuilder(TypeDefinitionRegistry typeDefinitionRegistry, GraphQLSchema graphQlSchema, JsonSchema jsonSchema) {
        return new SpecificationJsonSchemaBuilder(typeDefinitionRegistry, graphQlSchema, jsonSchema, null, null, null);
    }

    private static LinkedHashSet<String> objectOnlyJsonTypes = new LinkedHashSet<>();

    static {
        objectOnlyJsonTypes.add("object");
    }

    final TypeDefinitionRegistry typeDefinitionRegistry;
    final GraphQLSchema graphQlSchema;
    final JsonSchema jsonSchema;
    final SpecificationElementType parentSpecificationElementType;
    final JsonSchemaDefinitionElement element;
    final SpecificationElement specificationElement;

    SpecificationJsonSchemaBuilder(
            TypeDefinitionRegistry typeDefinitionRegistry,
            GraphQLSchema graphQLSchema,
            JsonSchema jsonSchema,
            SpecificationElementType parentSpecificationElementType,
            JsonSchemaDefinitionElement element,
            SpecificationElement specificationElement) {
        this.typeDefinitionRegistry = typeDefinitionRegistry;
        this.graphQlSchema = graphQLSchema;
        this.jsonSchema = jsonSchema;
        this.parentSpecificationElementType = parentSpecificationElementType;
        this.element = element;
        this.specificationElement = specificationElement;
    }

    public JsonSchemaBasedSpecification build() {
        SpecificationElement root = new SpecificationElementBuilder(element)
                .name("root")
                .parent(null)
                .specificationElementType(SpecificationElementType.ROOT)
                .jsonTypes(objectOnlyJsonTypes)
                .jsonSchema(jsonSchema)
                .build();
        return new JsonSchemaBasedSpecification(jsonSchema, root, typeDefinitionRegistry, graphQlSchema);
    }

}
