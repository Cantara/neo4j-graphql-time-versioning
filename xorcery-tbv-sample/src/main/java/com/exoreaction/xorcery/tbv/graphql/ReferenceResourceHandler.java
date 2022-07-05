package com.exoreaction.xorcery.tbv.graphql;

import com.exoreaction.xorcery.tbv.api.persistence.Transaction;
import com.exoreaction.xorcery.tbv.api.persistence.json.JsonDocument;
import com.exoreaction.xorcery.tbv.api.persistence.reactivex.RxJsonPersistence;
import com.exoreaction.xorcery.tbv.api.specification.Specification;
import com.exoreaction.xorcery.tbv.domain.reference.ReferenceJsonHelper;
import com.exoreaction.xorcery.tbv.domain.resource.ResourceContext;
import com.exoreaction.xorcery.tbv.domain.resource.ResourceElement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.huxhorn.sulky.ulid.ULID;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;

import static com.exoreaction.xorcery.tbv.api.persistence.json.JsonTools.mapper;
import static java.util.Optional.ofNullable;

public class ReferenceResourceHandler implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceResourceHandler.class);

    final RxJsonPersistence persistence;
    final Specification specification;
    final ResourceContext resourceContext;

    public ReferenceResourceHandler(RxJsonPersistence persistence, Specification specification, ResourceContext resourceContext) {
        this.persistence = persistence;
        this.specification = specification;
        this.resourceContext = resourceContext;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        if (exchange.getRequestMethod().equalToString("get")) {
            getReferenceTo(exchange);
        } else if (exchange.getRequestMethod().equalToString("put")) {
            putReferenceTo(exchange);
        } else if (exchange.getRequestMethod().equalToString("post")) {
            putReferenceTo(exchange);
        } else if (exchange.getRequestMethod().equalToString("delete")) {
            deleteReferenceTo(exchange);
        } else {
            exchange.setStatusCode(400);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send("Unsupported reference resource method: " + exchange.getRequestMethod());
        }
    }

    private void getReferenceTo(HttpServerExchange exchange) {
        ResourceElement topLevelElement = resourceContext.getFirstElement();

        JsonNode jsonNode;
        try (Transaction tx = persistence.createTransaction(true)) {
            JsonDocument jsonDocument = persistence.readDocument(tx, resourceContext.getTimestamp(), resourceContext.getNamespace(), topLevelElement.name(), topLevelElement.id()).blockingGet();
            if (jsonDocument == null || jsonDocument.deleted()) {
                exchange.setStatusCode(404);
                return;
            }
            jsonNode = jsonDocument.jackson();
        }

        boolean referenceToExists = resourceContext.referenceToExists(jsonNode);

        if (referenceToExists) {
            exchange.setStatusCode(200);
        } else {
            exchange.setStatusCode(404);
        }
    }

    private void putReferenceTo(HttpServerExchange exchange) {
        ResourceElement topLevelElement = resourceContext.getFirstElement();
        String namespace = resourceContext.getNamespace();
        String managedDomain = topLevelElement.name();
        String managedDocumentId = topLevelElement.id();

        exchange.getRequestReceiver().receiveFullString(
                (httpServerExchange, message) -> {
                    JsonDocument jsonDocument;
                    try (Transaction tx = persistence.createTransaction(true)) {
                        jsonDocument = persistence.readDocument(tx, resourceContext.getTimestamp(), namespace, managedDomain, managedDocumentId).blockingGet();
                    }
                    boolean referenceToExists = false;
                    if (jsonDocument != null && !jsonDocument.deleted()) {
                        referenceToExists = resourceContext.referenceToExists(jsonDocument.jackson());
                    }
                    if (referenceToExists) {
                        exchange.setStatusCode(200);
                    } else {
                        new ReferenceJsonHelper(specification, topLevelElement).createReferenceJson(resourceContext, jsonDocument.jackson());

                        String source = ofNullable(exchange.getQueryParameters().get("source")).map(Deque::peekFirst).orElse(null);
                        String sourceId = ofNullable(exchange.getQueryParameters().get("sourceId")).map(Deque::peekFirst).orElse(null);

                        ULID.Value txId = new ULID().nextValue();
                        SagaInput sagaInput = new SagaInput(txId, "PUT", "TODO", namespace, managedDomain, managedDocumentId, resourceContext.getTimestamp(), source, sourceId, jsonDocument.jackson());
                        WritePersistenceAPIs.createOrOverwrite(persistence, specification, sagaInput);

                        exchange.setStatusCode(200);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send("{\"tx-id\":\"" + txId + "\"}");
                    }
                },
                (exchange1, e) -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Error: " + e.getMessage());
                    LOG.warn("", e);
                },
                StandardCharsets.UTF_8);
    }

    private void deleteReferenceTo(HttpServerExchange exchange) {
        ResourceElement topLevelElement = resourceContext.getFirstElement();
        String namespace = resourceContext.getNamespace();
        String managedDomain = topLevelElement.name();
        String managedDocumentId = topLevelElement.id();

        ArrayNode output = mapper.createArrayNode();
        try (Transaction tx = persistence.createTransaction(true)) {
            JsonDocument jsonDocument = persistence.readDocument(tx, resourceContext.getTimestamp(), namespace, managedDomain, managedDocumentId).blockingGet();
            if (jsonDocument != null && !jsonDocument.deleted()) {
                output.add(jsonDocument.jackson());

            }
        }
        if (output.size() == 0) {
            exchange.setStatusCode(200);
            exchange.endExchange();
            return;
        }
        if (output.size() > 1) {
            throw new IllegalStateException("More than one document version match.");
        }
        // output.length() == 1
        JsonNode rootNode = output.get(0);
        boolean referenceToExists = resourceContext.referenceToExists(rootNode);
        if (!referenceToExists) {
            exchange.setStatusCode(200);
            exchange.endExchange();
            return;
        }

        new ReferenceJsonHelper(specification, resourceContext.getFirstElement()).deleteReferenceJson(resourceContext, rootNode);

        boolean sync = exchange.getQueryParameters().getOrDefault("sync", new LinkedList()).stream().anyMatch(s -> "true".equalsIgnoreCase((String) s));

        String source = ofNullable(exchange.getQueryParameters().get("source")).map(Deque::peekFirst).orElse(null);
        String sourceId = ofNullable(exchange.getQueryParameters().get("sourceId")).map(Deque::peekFirst).orElse(null);

        ULID.Value txId = new ULID().nextValue();
        SagaInput sagaInput = new SagaInput(txId, "PUT", "TODO", resourceContext.getNamespace(), resourceContext.getFirstElement().name(), resourceContext.getFirstElement().id(), resourceContext.getTimestamp(), source, sourceId, rootNode);
        WritePersistenceAPIs.createOrOverwrite(persistence, specification, sagaInput);

        if (sync) {
            exchange.setStatusCode(200);
            exchange.endExchange();
        } else {
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send("{\"tx-id\":\"" + txId + "\"}");
            exchange.endExchange();
        }
    }
}
