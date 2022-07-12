package com.exoreaction.xorcery.tbv.graphql;

import com.exoreaction.xorcery.tbv.api.persistence.reactivex.RxJsonPersistence;
import com.exoreaction.xorcery.tbv.api.specification.Specification;
import com.exoreaction.xorcery.tbv.graphql.directives.TBVDirectives;
import com.exoreaction.xorcery.tbv.neo4j.EmbeddedNeo4jInitializer;
import com.exoreaction.xorcery.tbv.neo4j.EmbeddedNeo4jPersistence;
import com.exoreaction.xorcery.tbv.neo4j.graphql.GraphQLNeo4jTBVLanguage;
import com.exoreaction.xorcery.tbv.neo4j.graphql.GraphQLNeo4jTBVSchemas;
import com.exoreaction.xorcery.tbv.neo4j.graphql.jsonschema.GraphQLToJsonConverter;
import com.exoreaction.xorcery.tbv.schema.JsonSchema;
import com.exoreaction.xorcery.tbv.schema.JsonSchema04Builder;
import com.exoreaction.xorcery.tbv.specification.JsonSchemaBasedSpecification;
import com.exoreaction.xorcery.tbv.specification.SpecificationJsonSchemaBuilder;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.SchemaPrinter;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.StatusCodes;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class UndertowApplication {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowApplication.class);
    private final RxJsonPersistence persistence;
    private final Specification specification;
    private final Undertow server;
    private final String host;
    private final int port;

    UndertowApplication(Specification specification,
                        RxJsonPersistence persistence,
                        String host,
                        int port,
                        String graphqlSchemaLocation,
                        NamespaceController namespaceController) {
        this.specification = specification;
        this.host = host;
        this.port = port;
        this.persistence = persistence;

        LOG.info("Initializing Http handlers ...");

        boolean enableRequestDump = false;
        boolean enableAccessLog = false;
        String accessLogFormat = "%h \"%r\" %s %b";
        String pathPrefix = "/";

        PathHandler pathHandler = Handlers.path();

        LOG.info("Initializing GraphQL Web API ...");

        File graphQLFile = new File(graphqlSchemaLocation);

        TypeDefinitionRegistry definitionRegistry = parseSchemaFile(graphQLFile);

        HttpHandler graphQLHttpHandler;

        LOG.info("Initializing GraphQL Neo4j integration ...");

        GraphQLSchema schema = GraphQLNeo4jTBVSchemas.schemaOf(GraphQLNeo4jTBVLanguage.transformRegistry(definitionRegistry, true));

        SchemaPrinter schemaPrinter = new SchemaPrinter();
        System.out.printf("  --- BEGIN SCHEMA ---%n%s%n  --- END SCHEMA ---", schemaPrinter.print(schema));

        graphQLHttpHandler = new GraphQLNeo4jHttpHandler(schema, GraphQLNeo4jTBVSchemas.domains(definitionRegistry), persistence);

        pathHandler.addExactPath("/graphql", graphQLHttpHandler);

        pathHandler.addExactPath("/graphiql", Handlers.resource(new ClassPathResourceManager(
                Thread.currentThread().getContextClassLoader(), "com/exoreaction/xorcery/tbv/graphql"
        )).setDirectoryListingEnabled(false).addWelcomeFiles("graphiql.html"));

        pathHandler.addPrefixPath("/", namespaceController);

        HttpHandler httpHandler = pathHandler;

        if (enableAccessLog) {
            httpHandler = new AccessLogHandler(httpHandler, new Slf4jAccessLogReceiver(LoggerFactory.getLogger("com.exoreaction.xorcery.tbv.accesslog")), accessLogFormat, Undertow.class.getClassLoader());
        }

        if (enableRequestDump) {
            LOG.info("Initializing request-dump ...");
            httpHandler = Handlers.requestDump(httpHandler);
        }

        if (pathPrefix != null && !pathPrefix.isEmpty()) {
            LOG.info("Using http prefix: {}", pathPrefix);
            httpHandler = Handlers.path(ResponseCodeHandler.HANDLE_404).addPrefixPath(pathPrefix, httpHandler);
        }

        LOG.info("Initializing CORS-handler ...");

        List<Pattern> corsAllowOrigin = Stream.of(".*".split(",")).map(Pattern::compile).collect(Collectors.toUnmodifiableList());
        Set<String> corsAllowMethods = Set.of("POST,GET,PUT,DELETE,HEAD".split(","));
        Set<String> corsAllowHeaders = Set.of("Content-Type,Authorization".split(","));
        boolean corsAllowCredentials = false;
        int corsMaxAge = 900;

        CORSHandler corsHandler = new CORSHandler(httpHandler, httpHandler, corsAllowOrigin,
                corsAllowCredentials, StatusCodes.NO_CONTENT, corsMaxAge, corsAllowMethods, corsAllowHeaders
        );

        LOG.info("Initializing Undertow ...");

        this.server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(corsHandler)
                .build();
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

    public static UndertowApplication initializeUndertowApplication(int port, String graphqlSchemaLocation, String neo4jEmbeddedDataFolder, String namespace) {
        LOG.info("Initializing Undertow server ...");

        LOG.info("Initializing specification ...");

        JsonSchemaBasedSpecification specification;

        File graphQLFile = new File(graphqlSchemaLocation);

        LOG.info("Using GraphQL file: {}", graphQLFile);

        TypeDefinitionRegistry definitionRegistry = parseSchemaFile(graphQLFile);

        GraphQLSchema schema;

        LOG.info("Transforming GraphQL schema to conform with GRANDstack compatible Neo4j modelling for Specification purposes");
        schema = GraphQLNeo4jTBVSchemas.schemaOf(GraphQLNeo4jTBVLanguage.transformRegistry(definitionRegistry, false)).transform(builder -> {
            builder.additionalDirectives(Set.of(
                    TBVDirectives.DOMAIN,
                    TBVDirectives.LINK,
                    TBVDirectives.REVERSE_LINK
            ));
        });

        GraphQLToJsonConverter graphQLToJsonConverter = new GraphQLToJsonConverter(definitionRegistry, schema);
        LinkedHashMap<String, JSONObject> jsonMap = graphQLToJsonConverter.createSpecification(schema);

        if (LOG.isTraceEnabled()) {
            jsonMap.entrySet().forEach(entry -> LOG.trace("JSON SCHEMA for type '{}': {}", entry.getKey(), entry.getValue().toString()));
        }

        specification = createJsonSpecification(definitionRegistry, schema, jsonMap);

        LOG.info("Initializing persistence ...");

        Map<String, String> map = Map.of(
                "neo4j.driver.url", "neo4j://0.0.0.0:7687",
                "neo4j.driver.username", "neo4j",
                "neo4j.driver.password", "PasSW0rd",
                "neo4j.cypher.show", "false",
                "neo4j.schema.drop-existing-indexes", "false",
                "neo4j.embedded.data.folder", neo4jEmbeddedDataFolder
        );
        EmbeddedNeo4jPersistence persistence = new EmbeddedNeo4jInitializer().initialize(namespace, map, specification.getManagedDomains(), specification);

        LOG.info("Initializing namespace-controller ...");

        NamespaceController namespaceController = new NamespaceController(
                namespace,
                specification,
                specification,
                persistence
        );

        String host = "localhost";

        return new UndertowApplication(specification, persistence, host, port, graphqlSchemaLocation, namespaceController);
    }

    private static JsonSchemaBasedSpecification createJsonSpecification(TypeDefinitionRegistry typeDefinitionRegistry, GraphQLSchema graphQlSchema, LinkedHashMap<String, JSONObject> jsonMap) {
        JsonSchemaBasedSpecification jsonSchemaBasedSpecification = null;
        Set<Map.Entry<String, JSONObject>> entries = jsonMap.entrySet();
        Iterator<Map.Entry<String, JSONObject>> iterator = entries.iterator();

        JsonSchema jsonSchema = null;
        while (iterator.hasNext()) {
            Map.Entry item = iterator.next();
            jsonSchema = new JsonSchema04Builder(jsonSchema, item.getKey().toString(), item.getValue().toString()).build();
            jsonSchemaBasedSpecification = SpecificationJsonSchemaBuilder.createBuilder(typeDefinitionRegistry, graphQlSchema, jsonSchema).build();
        }

        return jsonSchemaBasedSpecification;
    }

    public static Map<String, String> subMapFromPrefix(Map<String, String> configMap, String prefix) {
        NavigableMap<String, String> navConf;
        if (configMap instanceof NavigableMap) {
            navConf = (NavigableMap) configMap;
        } else {
            navConf = new TreeMap<>(configMap);
        }
        return navConf.subMap(prefix, true, prefix + "\uFFFF", false)
                .entrySet().stream().collect(Collectors.toMap(e -> e.getKey().substring(prefix.length()), Map.Entry::getValue));
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void start() {
        LOG.info("Starting Undertow ...");
        server.start();
        LOG.info("Started Time Based Versioning document server. PID {}", ProcessHandle.current().pid());
        LOG.info("Listening on {}:{}", host, port);
    }

    public void stop() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(CompletableFuture.runAsync(() -> {
            server.stop();
            LOG.debug("Undertow was shutdown");
        }));
        futures.add(CompletableFuture.runAsync(() -> {
            persistence.close();
            LOG.debug("Persistence provider was shutdown");
        }));
        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            all.orTimeout(10, TimeUnit.SECONDS).join();
            LOG.debug("All internal services was shutdown.");
        } catch (CompletionException e) {
            if (e.getCause() != null && e.getCause() instanceof TimeoutException) {
                LOG.warn("Timeout before shutdown of internal services could complete.");
            } else {
                LOG.error("Error while waiting for all services to shut down.", e);
            }
        }
        LOG.info("Leaving.. Bye!");
    }

    public Undertow getServer() {
        return server;
    }

    public RxJsonPersistence getPersistence() {
        return persistence;
    }

    public Undertow getUndertowServer() {
        return server;
    }

    public Specification getSpecification() {
        return specification;
    }
}
