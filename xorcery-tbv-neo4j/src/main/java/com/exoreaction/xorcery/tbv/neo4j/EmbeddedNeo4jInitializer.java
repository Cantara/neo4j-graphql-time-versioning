package com.exoreaction.xorcery.tbv.neo4j;

import apoc.cypher.CypherFunctions;
import apoc.schema.Schemas;
import com.exoreaction.xorcery.tbv.api.persistence.PersistenceInitializer;
import com.exoreaction.xorcery.tbv.api.persistence.ProviderName;
import com.exoreaction.xorcery.tbv.api.specification.Specification;
import com.exoreaction.xorcery.tbv.neo4j.apoc.path.PathExplorer;
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
import org.neo4j.exceptions.KernelException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.api.procedure.GlobalProcedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

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
                "neo4j.embedded.data.folder",
                "neo4j.schema.drop-existing-indexes"
        );
    }

    @Override
    public EmbeddedNeo4jPersistence initialize(String defaultNamespace, Map<String, String> configuration, Set<String> managedDomains, Specification specification) {
        JavaUtilLoggingInitializer.initialize();

        Path home = Path.of(configuration.get("neo4j.embedded.data.folder"));
        Map<String, String> settings = Map.of("dbms.security.procedures.unrestricted", "apoc.*,tbv.*");
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(home)
                .setConfigRaw(settings)
                .build();

        String database = "neo4j";
        GraphDatabaseService graphDb;
        try {
            graphDb = managementService.database(database);
        } catch (DatabaseNotFoundException e) {
            managementService.createDatabase(database);
            graphDb = managementService.database(database);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(managementService::shutdown));

        registerProcedure(graphDb, Schemas.class);
        registerProcedure(graphDb, PathExplorer.class);
        registerProcedure(graphDb, CypherFunctions.class);

        boolean logCypher = Boolean.parseBoolean(configuration.get("neo4j.cypher.show"));
        boolean dropExistingIndexes = Boolean.parseBoolean(ofNullable(configuration.get("neo4j.schema.drop-existing-indexes")).orElse("false"));
        EmbeddedNeo4jTransactionFactory transactionFactory = new EmbeddedNeo4jTransactionFactory(graphDb, logCypher);
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
        EmbeddedNeo4jIndexManagement indexManagement = new EmbeddedNeo4jIndexManagement(defaultNamespace, managedDomains, customIndexes, dropExistingIndexes);
        try (EmbeddedNeo4jTransaction tx = transactionFactory.createTransaction(false)) {
            indexManagement.createIdIndices(tx);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }

        return new EmbeddedNeo4jPersistence(transactionFactory);
    }

    public static void registerProcedure(GraphDatabaseService db, Class<?>... procedures) {
        GlobalProcedures globalProcedures = ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(GlobalProcedures.class);
        for (Class<?> procedure : procedures) {
            try {
                globalProcedures.registerProcedure(procedure, true);
                globalProcedures.registerFunction(procedure, true);
                globalProcedures.registerAggregationFunction(procedure, true);
            } catch (KernelException e) {
                throw new RuntimeException("while registering " + procedure, e);
            }
        }
    }
}
