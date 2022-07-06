package com.exoreaction.xorcery.tbv.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import no.cantara.stingray.httpclient.StingrayHttpClient;
import no.cantara.stingray.httpclient.StingrayHttpClients;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

public class TBVClient {

    private final String namespace;
    private final String host;
    private final int port;

    private StingrayHttpClient client;

    public TBVClient(String namespace, String host, int port) {
        this.namespace = namespace;
        this.host = host;
        this.port = port;
        client = StingrayHttpClients.factory()
                .newClient()
                .useTarget(target -> target.withScheme("http")
                        .withHost(host)
                        .withPort(port))
                .useConfiguration(config -> config.withDefaultHeader("origin", "localhost"))
                .build();
    }

    public JsonNode sendGraphQLQuery(Consumer<GraphQLQueryRequestBuilder> consumer) {
        GraphQLQueryRequestBuilder builder = new GraphQLQueryRequestBuilder();
        consumer.accept(builder);
        JsonNode body = builder.build();
        System.out.printf("POST /graphql%n%s%n", body.toPrettyString());

        JsonNode response = client.post()
                .path("/graphql")
                .bodyJson(body.toPrettyString())
                .execute().isSuccessful()
                .contentAs((String str) -> TestUtils.mapper.readTree(str));

        System.out.printf("RESPONSE%n%s%n", response.toPrettyString());

        return response;
    }

    public void checkNotFound(String entity, String resourceId, ZonedDateTime timestamp) {
        client.get()
                .path(namespace + "/" + entity + "/" + resourceId)
                .query("timestamp", timestamp.toString())
                .execute()
                .hasStatusCode(404);
    }

    public String read(String entity, String resourceId, ZonedDateTime timestamp) {
        String document = client.get()
                .path(namespace + "/" + entity + "/" + resourceId)
                .query("timestamp", timestamp.toString())
                .execute()
                .hasStatusCode(200)
                .contentAsString();
        return document;
    }

    public void write(String entity, String resourceId, ZonedDateTime timestamp, String document) {
        client.put()
                .path(namespace + "/" + entity + "/" + resourceId)
                .query("timestamp", timestamp.toString())
                .bodyJson(document)
                .execute()
                .isSuccessful();
    }
}
