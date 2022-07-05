package com.exoreaction.xorcery.tbv.neo4j;

import com.exoreaction.xorcery.tbv.api.persistence.PersistenceInitializer;
import com.exoreaction.xorcery.tbv.api.persistence.ProviderName;
import com.exoreaction.xorcery.tbv.api.specification.Specification;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.schema.SchemaTraverser;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.dbms.api.DatabaseNotFoundException;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static com.exoreaction.xorcery.tbv.neo4j.EmbeddedNeo4jInitializer.PROVIDER_ALIAS;
import static java.util.Optional.ofNullable;

@ProviderName(PROVIDER_ALIAS)
public class EmbeddedNeo4jInitializer implements PersistenceInitializer {

    public static final String PROVIDER_ALIAS = "neo4j-embedded";

    static class JavaUtilLoggingInitializer {
        static {
            JavaUtilLoggerBridge.installJavaUtilLoggerBridgeHandler(Level.INFO);
        }

        static void initialize() {
        }
    }

    @Override
    public String persistenceProviderId() {
        return PROVIDER_ALIAS;
    }

    @Override
    public Set<String> configurationKeys() {
        return Set.of(
                "neo4j.cypher.show",
                "neo4j.driver.url",
                "neo4j.driver.username",
                "neo4j.driver.password"
        );
    }

    @Override
    public Neo4jPersistence initialize(String defaultNamespace, Map<String, String> configuration, Set<String> managedDomains, Specification specification) {
        JavaUtilLoggingInitializer.initialize();

        Path home = Path.of("target/neo4j/sampledata");
        Map<String, String> settings = Map.of();
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(home)
                .setConfigRaw(settings)
                .build();

        String database = "junit";
        GraphDatabaseService graphDb = null;
        try {
            graphDb = managementService.database(database);
        } catch (DatabaseNotFoundException e) {
            managementService.createDatabase(database);
            graphDb = managementService.database(database);
        }

        boolean logCypher = Boolean.parseBoolean(configuration.get("neo4j.cypher.show"));
        boolean dropExistingIndexes = Boolean.parseBoolean(ofNullable(configuration.get("neo4j.schema.drop-existing-indexes")).orElse("false"));
        Neo4jTransactionFactory transactionFactory = new EmbeddedNeo4jTransactionFactory(graphDb, logCypher);
        Map<String, Set<String>> customIndexes = new LinkedHashMap<>();

        ofNullable(specification)
                .map(Specification::schema).
                ifPresent(schema -> {
                    final SchemaTraverser TRAVERSER = new SchemaTraverser();
                    TRAVERSER.depthFirst(new GraphQLTypeVisitorStub() {
                        @Override
                        public TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context) {
                            for (GraphQLFieldDefinition fieldDefinition : node.getFieldDefinitions()) {
                                GraphQLDirective indexDirective = fieldDefinition.getDirective("index");
                                if (indexDirective != null) {
                                    customIndexes.computeIfAbsent(node.getName(), k -> new LinkedHashSet<>()).add(fieldDefinition.getName());
                                }
                            }
                            return super.visitGraphQLObjectType(node, context);
                        }
                    }, schema.getAllTypesAsList());
                });
        Neo4jIndexManagement indexManagement = new Neo4jIndexManagement(defaultNamespace, managedDomains, customIndexes, dropExistingIndexes);
        try (Neo4jTransaction tx = transactionFactory.createTransaction(false)) {
            indexManagement.createIdIndices(tx);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }

        return new Neo4jPersistence(transactionFactory);
    }

    private static Driver open(String neo4jDriverURL, String neo4jDriverUsername, String neo4jDriverPassword) {
        return GraphDatabase.driver(neo4jDriverURL, AuthTokens.basic(neo4jDriverUsername, neo4jDriverPassword));
    }
}
