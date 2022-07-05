package com.exoreaction.xorcery.tbv.graphql;

import com.exoreaction.xorcery.tbv.api.persistence.Transaction;
import com.exoreaction.xorcery.tbv.api.persistence.json.JsonDocument;
import com.exoreaction.xorcery.tbv.api.persistence.json.JsonTools;
import com.exoreaction.xorcery.tbv.api.persistence.reactivex.RxJsonPersistence;
import com.exoreaction.xorcery.tbv.api.specification.Specification;
import com.exoreaction.xorcery.tbv.domain.BodyParser;
import com.exoreaction.xorcery.tbv.domain.resource.ResourceContext;
import com.exoreaction.xorcery.tbv.domain.resource.ResourceElement;
import com.exoreaction.xorcery.tbv.schema.SchemaRepository;
import com.exoreaction.xorcery.tbv.validation.LinkedDocumentValidationException;
import com.exoreaction.xorcery.tbv.validation.LinkedDocumentValidator;
import com.fasterxml.jackson.databind.JsonNode;
import de.huxhorn.sulky.ulid.ULID;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Deque;

import static com.exoreaction.xorcery.tbv.api.persistence.json.JsonTools.mapper;
import static java.util.Optional.ofNullable;

public class EmbeddedResourceHandler implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedResourceHandler.class);

    private final Specification specification;
    private final SchemaRepository schemaRepository;
    private final ResourceContext resourceContext;
    private final RxJsonPersistence persistence;

    public EmbeddedResourceHandler(RxJsonPersistence persistence, Specification specification, SchemaRepository schemaRepository, ResourceContext resourceContext) {
        this.persistence = persistence;
        this.specification = specification;
        this.schemaRepository = schemaRepository;
        this.resourceContext = resourceContext;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        if (exchange.getRequestMethod().equalToString("get")) {
            getEmbedded(exchange);
        } else if (exchange.getRequestMethod().equalToString("put")) {
            putEmbedded(exchange);
        } else if (exchange.getRequestMethod().equalToString("post")) {
            putEmbedded(exchange);
        } else if (exchange.getRequestMethod().equalToString("delete")) {
            deleteEmbedded(exchange);
        } else {
            exchange.setStatusCode(400);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send("Unsupported embedded resource method: " + exchange.getRequestMethod());
        }
    }

    private void getEmbedded(HttpServerExchange exchange) {
        ResourceElement topLevelElement = resourceContext.getFirstElement();

        JsonNode jsonNode;
        try (Transaction tx = persistence.createTransaction(true)) {
            JsonDocument jsonDocument = persistence.readDocument(tx, resourceContext.getTimestamp(), resourceContext.getNamespace(), topLevelElement.name(), topLevelElement.id()).blockingGet();
            jsonNode = ofNullable(jsonDocument).map(JsonDocument::jackson).orElse(null);
        }

        if (jsonNode == null) {
            exchange.setStatusCode(404);
            return;
        }

        // TODO consistent API independent of sub-tree json type. i.e. figure out whether we should always wrap
        // TODO result in a json-array?
        JsonNode subTreeRoot = resourceContext.subTree(jsonNode);
        String result;
        if (subTreeRoot == null) {
            result = "[null]";
        } else if (subTreeRoot.isContainerNode()) {
            result = JsonTools.toJson(subTreeRoot);
        } else {
            // wrap simple values in json array.
            result = JsonTools.toJson(mapper.createArrayNode().add(subTreeRoot));
        }
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; charset=utf-8");
        exchange.getResponseSender().send(result, StandardCharsets.UTF_8);
    }

    private void putEmbedded(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString(
                (httpServerExchange, message) -> {
                    ResourceElement topLevelElement = resourceContext.getFirstElement();
                    String namespace = resourceContext.getNamespace();
                    String managedDomain = topLevelElement.name();
                    String managedDocumentId = topLevelElement.id();

                    JsonNode managedDocument;
                    try (Transaction tx = persistence.createTransaction(true)) {
                        JsonDocument jsonDocument = persistence.readDocument(tx, resourceContext.getTimestamp(), namespace, managedDomain, managedDocumentId).blockingGet();
                        managedDocument = ofNullable(jsonDocument).map(JsonDocument::jackson).orElse(null);
                    }

                    if (managedDocument == null) {
                        exchange.setStatusCode(404);
                        return;
                    }

                    String contentType = ofNullable(exchange.getRequestHeaders().get(Headers.CONTENT_TYPE))
                            .map(HeaderValues::getFirst).orElse("application/json");
                    JsonNode embeddedJson = BodyParser.deserializeBody(contentType, message);

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("{} {}\n{}", exchange.getRequestMethod(), exchange.getRequestPath(), message);
                    }

                    mergeJson(resourceContext, managedDocument, embeddedJson);

                    try {
                        LinkedDocumentValidator validator = new LinkedDocumentValidator(specification, schemaRepository);
                        // TODO avoid serialization and de-serialization due to using both jackson and org.json
                        validator.validate(managedDomain, JsonTools.toJson(managedDocument));
                    } catch (LinkedDocumentValidationException ve) {
                        LOG.debug("Schema validation error: {}", ve.getMessage());
                        exchange.setStatusCode(400);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Schema validation error: " + ve.getMessage());
                        return;
                    }

                    String source = ofNullable(exchange.getQueryParameters().get("source")).map(Deque::peekFirst).orElse(null);
                    String sourceId = ofNullable(exchange.getQueryParameters().get("sourceId")).map(Deque::peekFirst).orElse(null);

                    ULID.Value txId = new ULID().nextValue();
                    SagaInput sagaInput = new SagaInput(txId, "PUT", "TODO", namespace, managedDomain, managedDocumentId, resourceContext.getTimestamp(), source, sourceId, managedDocument);
                    WritePersistenceAPIs.createOrOverwrite(persistence, specification, sagaInput);

                    exchange.setStatusCode(200);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send("{\"tx-id\":\"" + txId + "\"}");
                },
                (exchange1, e) -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Error putting embedded resource: " + e.getMessage());
                    LOG.warn("", e);
                },
                StandardCharsets.UTF_8);
    }

    private void deleteEmbedded(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString(
                (httpServerExchange, message) -> {
                    ResourceElement topLevelElement = resourceContext.getFirstElement();
                    String namespace = resourceContext.getNamespace();
                    String managedDomain = topLevelElement.name();
                    String managedDocumentId = topLevelElement.id();

                    JsonNode rootNode;
                    try (Transaction tx = persistence.createTransaction(true)) {
                        JsonDocument jsonDocument = persistence.readDocument(tx, resourceContext.getTimestamp(), namespace, managedDomain, managedDocumentId).blockingGet();
                        rootNode = ofNullable(jsonDocument).map(JsonDocument::jackson).orElse(null);
                    }

                    if (rootNode == null) {
                        exchange.setStatusCode(404);
                        return;
                    }

                    mergeJson(resourceContext, rootNode, null);

                    String source = ofNullable(exchange.getQueryParameters().get("source")).map(Deque::peekFirst).orElse(null);
                    String sourceId = ofNullable(exchange.getQueryParameters().get("sourceId")).map(Deque::peekFirst).orElse(null);

                    ULID.Value txId = new ULID().nextValue();
                    SagaInput sagaInput = new SagaInput(txId, "PUT", "TODO", namespace, managedDomain, managedDocumentId, resourceContext.getTimestamp(), source, sourceId, rootNode);
                    WritePersistenceAPIs.createOrOverwrite(persistence, specification, sagaInput);

                    exchange.setStatusCode(200);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send("{\"tx-id\":\"" + txId + "\"}");
                },
                (exchange1, e) -> {
                    exchange.setStatusCode(500);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Error deleting embedded resource: " + e.getMessage());
                    LOG.warn("", e);
                },
                StandardCharsets.UTF_8);
    }

    public boolean mergeJson(ResourceContext resourceContext, JsonNode documentRootNode, JsonNode subTree) {
        return resourceContext.navigateAndCreateJson(documentRootNode, t -> {
            String embeddedPropertyName = t.resourceElement.name();
            if (t.jsonObject.isArray()) {
                // TODO support array-navigation
                throw new UnsupportedOperationException("array navigation not supported");
            }
            if (subTree == null) {
                t.jsonObject.remove(embeddedPropertyName);
                return true;
            }
            t.jsonObject.set(embeddedPropertyName, subTree);
            return true;
        });
    }


}
