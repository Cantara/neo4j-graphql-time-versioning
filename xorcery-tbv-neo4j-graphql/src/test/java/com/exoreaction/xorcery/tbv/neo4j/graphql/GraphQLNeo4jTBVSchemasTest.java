package com.exoreaction.xorcery.tbv.neo4j.graphql;

import com.exoreaction.xorcery.tbv.graphql.directives.TBVDirectives;
import com.exoreaction.xorcery.tbv.neo4j.graphqltocypher.TimeVersioningGraphQLToCypherTranslator;
import graphql.ExecutionInput;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.SchemaPrinter;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.neo4j.graphql.Cypher;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;

public class GraphQLNeo4jTBVSchemasTest {

    @Test
    public void thatUserAndGroupGraphQLQueryCanBeTranslatedToCorrectCypher() {
        File schemaFile = new File("src/test/resources/schema-samples/userandgroup.graphql");
        TypeDefinitionRegistry definitionRegistry = parseSchemaFile(schemaFile);
        GraphQLSchema schema = GraphQLNeo4jTBVSchemas.schemaOf(GraphQLNeo4jTBVLanguage.transformRegistry(definitionRegistry, false)).transform(builder -> {
            builder.additionalDirectives(Set.of(
                    TBVDirectives.DOMAIN,
                    TBVDirectives.LINK,
                    TBVDirectives.REVERSE_LINK
            ));
        });

        System.out.println();
        System.out.printf("   --- ::BEGIN SCHEMA:: ---%n");
        System.out.printf("%s%n", new SchemaPrinter().print(schema));
        System.out.printf("   --- ::END SCHEMA:: ---%n");
        System.out.println();

        ExecutionInput.Builder executionInputBuilder = ExecutionInput.newExecutionInput()
                .query("query ($userId: String) {\n" +
                        "  user(id: $userId) {\n" +
                        "    name\n" +
                        "    group {\n" +
                        "      name\n" +
                        "    }\n" +
                        "  }\n" +
                        "}")
                .variables(Map.of("userId", "john"));

        ExecutionInput executionInput = executionInputBuilder.build();

        // translate query to Cypher and execute against neo4j
        System.out.printf("GraphQL BEFORE transformation:%n%s%n", executionInput.getQuery());
        String query = GraphQLQueryTransformer.addTimeBasedVersioningArgumentValues(schema, executionInput);
        System.out.printf("GraphQL AFTER transformation:%n%s%n", query);
        Map<String, Object> inputVariables = executionInput.getVariables();

        TimeVersioningGraphQLToCypherTranslator translator = new TimeVersioningGraphQLToCypherTranslator(schema, Set.of("User", "Group"));
        ZonedDateTime timeVersion = ZonedDateTime.now();
        List<Cypher> cyphers = translator.translate(query, inputVariables, timeVersion);

        for (Cypher cypher : cyphers) {
            Cypher c = new Cypher(cypher.component1(), cypher.component2(), cypher.component3(), cypher.component4());
            System.out.printf("1:%n%s%n", c.component1());
            System.out.printf("2:%n%s%n", c.component2());
            System.out.printf("3:%n%s%n", c.component3());
            System.out.printf("4:%n%s%n", c.component4());
        }
    }

    private static TypeDefinitionRegistry parseSchemaFile(File graphQLFile) {
        TypeDefinitionRegistry definitionRegistry;
        URL systemResource = ClassLoader.getSystemResource(graphQLFile.getPath());

        if (ofNullable(systemResource).isPresent()) {
            definitionRegistry = new SchemaParser().parse(new File(systemResource.getPath()));
        } else {
            definitionRegistry = new SchemaParser().parse(new File(graphQLFile.getPath()));
        }
        return definitionRegistry;
    }
}