package com.exoreaction.xorcery.tbv.neo4j.graphql;

import graphql.schema.GraphQLSchema;
import org.neo4j.graphql.Cypher;
import org.neo4j.graphql.OptimizedQueryException;
import org.neo4j.graphql.QueryContext;
import org.neo4j.graphql.SchemaBuilder;
import org.neo4j.graphql.Translator;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class GraphQLNeo4jBasicSchemasTest {

    @Test
    public void thatSomething() throws OptimizedQueryException {
        GraphQLSchema schema = SchemaBuilder.buildSchema("type Tenant {\n" +
                "                            id: String!\n" +
                "                            name: String!\n" +
                "                        }\n" +
                "                                        \n" +
                "                        type Query {\n" +
                "                                    tenant : [Tenant]\n" +
                "                                    tenantByName(name:String) : Tenant\n" +
                "                                }");

        String query = "query ($tenant: String!) {\n" +
                "  tenantByName(name: $tenant) {\n" +
                "    name\n" +
                "  }\n" +
                "}";

        QueryContext queryContext = new QueryContext(false);

        Translator translator = new Translator(schema);
        List<Cypher> cyphers = translator.translate(query, Map.of("tenant", "t1"), queryContext);

        for (Cypher cypher : cyphers) {
            System.out.printf("1:%n%s%n", cypher.component1());
            System.out.printf("2:%n%s%n", cypher.component2());
            System.out.printf("3:%n%s%n", cypher.component3());
            System.out.printf("4:%n%s%n", cypher.component4());
        }
    }
}