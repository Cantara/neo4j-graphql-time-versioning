package com.exoreaction.xorcery.tbv.graphql;

import com.exoreaction.xorcery.tbv.api.persistence.Transaction;
import com.exoreaction.xorcery.tbv.api.persistence.json.JsonDocument;
import com.exoreaction.xorcery.tbv.api.persistence.json.JsonTools;
import com.exoreaction.xorcery.tbv.api.persistence.reactivex.Range;
import com.exoreaction.xorcery.tbv.api.persistence.reactivex.RxJsonPersistence;
import com.exoreaction.xorcery.tbv.api.specification.Specification;
import com.exoreaction.xorcery.tbv.domain.BodyParser;
import com.exoreaction.xorcery.tbv.domain.resource.ResourceContext;
import com.exoreaction.xorcery.tbv.domain.resource.ResourceElement;
import com.exoreaction.xorcery.tbv.schema.SchemaRepository;
import com.exoreaction.xorcery.tbv.validation.LinkedDocumentValidationException;
import com.exoreaction.xorcery.tbv.validation.LinkedDocumentValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.huxhorn.sulky.ulid.ULID;
import io.reactivex.Flowable;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Deque;

import static com.exoreaction.xorcery.tbv.api.persistence.json.JsonTools.mapper;
import static java.util.Optional.ofNullable;

public class ManagedResourceHandler implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ManagedResourceHandler.class);

    private final RxJsonPersistence persistence;
    private final Specification specification;
    private final SchemaRepository schemaRepository;
    private final ResourceContext resourceContext;

    public ManagedResourceHandler(RxJsonPersistence persistence, Specification specification, SchemaRepository schemaRepository, ResourceContext resourceContext) {
        this.persistence = persistence;
        this.specification = specification;
        this.schemaRepository = schemaRepository;
        this.resourceContext = resourceContext;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        if (exchange.getRequestMethod().equalToString("get")) {
            getManaged(exchange);
        } else if (exchange.getRequestMethod().equalToString("put")) {
            putManaged(exchange);
        } else if (exchange.getRequestMethod().equalToString("post")) {
            putManaged(exchange);
        } else if (exchange.getRequestMethod().equalToString("delete")) {
            deleteManaged(exchange);
        } else {
            exchange.setStatusCode(400);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send("Unsupported managed resource method: " + exchange.getRequestMethod());
        }
    }

    private void getManaged(HttpServerExchange exchange) {
        ResourceElement topLevelElement = resourceContext.getFirstElement();

        boolean isManagedList = topLevelElement.id() == null;

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; charset=utf-8");

        if (isManagedList && exchange.getQueryParameters().containsKey("schema")) {
            String jsonSchema = schemaRepository.getJsonSchema().getSchemaJson(resourceContext.getFirstElement().name());
            exchange.getResponseSender().send(jsonSchema, StandardCharsets.UTF_8);
            return;
        }

        try (Transaction tx = persistence.createTransaction(true)) {
            if (isManagedList) {
                Iterable<JsonDocument> documents = persistence.readDocuments(tx, resourceContext.getTimestamp(), resourceContext.getNamespace(), topLevelElement.name(), Range.unbounded()).blockingIterable();
                ArrayNode output = mapper.createArrayNode();
                for (JsonDocument jsonDocument : documents) {
                    if (jsonDocument.deleted()) {
                        continue;
                    }
                    output.add(jsonDocument.jackson());
                }
                exchange.getResponseSender().send(JsonTools.toJson(output), StandardCharsets.UTF_8);
            } else {
                if (exchange.getQueryParameters().containsKey("timeline")) {
                    ArrayNode output = mapper.createArrayNode();
                    // TODO Support pagination or time-range based query parameters
                    Flowable<JsonDocument> jsonDocumentFlowable = persistence.readDocumentVersions(tx, resourceContext.getNamespace(), topLevelElement.name(), topLevelElement.id(), Range.unbounded());
                    for (JsonDocument jsonDocument : jsonDocumentFlowable.blockingIterable()) {
                        ObjectNode timeVersionedInstance = output.addObject();
                        timeVersionedInstance.put("version", jsonDocument.key().timestamp().toString());
                        timeVersionedInstance.set("document", jsonDocument.jackson());
                    }
                    if (output.size() == 0) {
                        exchange.setStatusCode(StatusCodes.NOT_FOUND).endExchange();
                        return;
                    }
                    exchange.getResponseSender().send(JsonTools.toJson(output), StandardCharsets.UTF_8);
                } else {
                    JsonDocument jsonDocument = persistence.readDocument(tx, resourceContext.getTimestamp(), resourceContext.getNamespace(), topLevelElement.name(), topLevelElement.id()).blockingGet();
                    if (jsonDocument != null && !jsonDocument.deleted()) {
                        exchange.getResponseSender().send(JsonTools.toJson(jsonDocument.jackson()), StandardCharsets.UTF_8);
                    } else {
                        exchange.setStatusCode(StatusCodes.NOT_FOUND);
                    }
                }
            }
        }
        exchange.endExchange();
    }

    private void putManaged(HttpServerExchange exchange) {
        ResourceElement topLevelElement = resourceContext.getFirstElement();
        String namespace = resourceContext.getNamespace();
        String managedDomain = topLevelElement.name();
        String managedDocumentId = topLevelElement.id();

        exchange.getRequestReceiver().receiveFullString(
                (httpServerExchange, requestBody) -> {
                    // check if we received an empty payload
                    if ("".equals(requestBody)) {
                        LOG.error("Received empty payload for: {}", exchange.getRequestPath());
                        exchange.setStatusCode(400);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Payload was empty!");
                        return;
                    }

                    // check that we have a document id.
                    if (managedDocumentId == null || "".equals(managedDocumentId)) {
                        exchange.setStatusCode(400);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Id was empty!");
                        return;
                    }

                    String contentType = ofNullable(exchange.getRequestHeaders().get(Headers.CONTENT_TYPE))
                            .map(HeaderValues::getFirst).orElse("application/json");
                    JsonNode requestData = BodyParser.deserializeBody(contentType, requestBody);

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("{} {}\n{}", exchange.getRequestMethod(), exchange.getRequestPath(), requestBody);
                    }

                    try {
                        LinkedDocumentValidator validator = new LinkedDocumentValidator(specification, schemaRepository);
                        validator.validate(managedDomain, requestBody);
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
                    SagaInput sagaInput = new SagaInput(txId, "PUT", "TODO", namespace, managedDomain, managedDocumentId, resourceContext.getTimestamp(), source, sourceId, requestData);
                    WritePersistenceAPIs.createOrOverwrite(persistence, specification, sagaInput);

                    exchange.setStatusCode(StatusCodes.CREATED);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send("{\"tx-id\":\"" + txId + "\"}");
                },
                (exchange1, e) -> {
                    exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Error: " + e.getMessage());
                    LOG.warn("", e);
                },
                StandardCharsets.UTF_8);
    }

    private void deleteManaged(HttpServerExchange exchange) {
        ResourceElement topLevelElement = resourceContext.getFirstElement();
        String managedDomain = topLevelElement.name();

        String source = ofNullable(exchange.getQueryParameters().get("source")).map(Deque::peekFirst).orElse(null);
        String sourceId = ofNullable(exchange.getQueryParameters().get("sourceId")).map(Deque::peekFirst).orElse(null);

        ULID.Value txId = new ULID().nextValue();
        SagaInput sagaInput = new SagaInput(txId, "DELETE", "TODO", resourceContext.getNamespace(), managedDomain, topLevelElement.id(), resourceContext.getTimestamp(), source, sourceId, null);
        WritePersistenceAPIs.markAsDeleted(persistence, specification, sagaInput);

        HeaderMap responseHeaders = exchange.getResponseHeaders();
        exchange.setStatusCode(StatusCodes.OK);
        responseHeaders.put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("{\"tx-id\":\"" + txId + "\"}");
    }
}
