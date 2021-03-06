package com.exoreaction.xorcery.tbv.neo4j.graphql.jsonschema;

import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLSchema;
import graphql.schema.SchemaTraverser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class GraphQLToJsonConverter {
    private static final Logger LOG = LoggerFactory.getLogger(GraphQLToJsonConverter.class);
    private static final SchemaTraverser TRAVERSER = new SchemaTraverser();
    private final TypeDefinitionRegistry typeDefinitionRegistry;
    private final GraphQLSchema graphQLSchema;

    public GraphQLToJsonConverter(TypeDefinitionRegistry typeDefinitionRegistry, GraphQLSchema schema) {
        this.typeDefinitionRegistry = typeDefinitionRegistry;
        this.graphQLSchema = schema;
    }

    public LinkedHashMap<String, JSONObject> createSpecification(GraphQLSchema graphQLSchema) {
        Map<String, GraphQLNamedType> typeMap = graphQLSchema.getTypeMap();
        LinkedHashMap<String, JSONObject> jsonMap = new LinkedHashMap<>();
        StringBuilder sb = new StringBuilder();

        JsonSchemaGenerator parseObjectTypesVisitor = new JsonSchemaGenerator(typeDefinitionRegistry, jsonMap);
        TRAVERSER.depthFirst(parseObjectTypesVisitor, typeMap.values());
        parseObjectTypesVisitor.resolveDomainReferences();

        jsonMap.forEach((managedDomain, jsonObject) -> sb.append(" /" + managedDomain));

        LOG.info("{}", (sb.length() == 0 ? "No schemas configured!" : "Managed domains: " + sb.substring(1)));

        return jsonMap;
    }
}
