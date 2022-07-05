package com.exoreaction.xorcery.tbv.graphql;

import no.cantara.stingray.httpclient.StingrayHttpClient;
import no.cantara.stingray.httpclient.StingrayHttpClients;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;

import static org.testng.Assert.assertEquals;

public class TimeBasedVersioningTest {

    @Test
    public void thatBasicReadThenWriteWorks() {
        UndertowApplication application = UndertowApplication.initializeUndertowApplication(9090);
        application.start();

        StingrayHttpClient client = StingrayHttpClients.factory()
                .newClient()
                .useTarget(target -> target.withScheme("http")
                        .withHost(application.getHost())
                        .withPort(application.getPort()))
                .useConfiguration(config -> config.withDefaultHeader("origin", "localhost"))
                .build();

        write(client, "User", "john", ZonedDateTime.now().minusDays(10), """
                {
                    "id" : "john",
                    "name" : "John Smith",
                    "group" : ["/Group/sw"]
                }
                """);

        write(client, "Group", "RnD", ZonedDateTime.now().minusDays(5), """
                {
                    "id" : "RnD",
                    "name" : "Research & Development"
                }""");

        write(client, "Group", "sw", ZonedDateTime.now().minusDays(4), """
                {
                    "id" : "sw",
                    "name" : "Software",
                    "parent": ["/Group/RnD"]
                }
                """);

        write(client, "Group", "pm", ZonedDateTime.now().minusDays(4), """
                {
                    "id" : "pm",
                    "name" : "Product Managment",
                    "parent": ["/Group/RnD"]
                }
                """);

        write(client, "Group", "RnD", ZonedDateTime.now().minusDays(3), """
                {
                    "id" : "RnD",
                    "name" : "Research & Donuts"
                }
                """);

        write(client, "User", "john/name", ZonedDateTime.now().minusMinutes(5), """
                "John Johnson"
                """);

        // schedule a name-change in the future, 3 months from now
        write(client, "User", "john/name", ZonedDateTime.now().plusMonths(3), """
                "Jack Johnson"
                """);

        // did not exist a month ago
        checkNotFound(client, "User", "john", ZonedDateTime.now().minusMonths(1));

        // named "John Smith" two hours ago
        assertEquals(read(client, "User", "john", ZonedDateTime.now().minusHours(2)), """
                {"id":"john","name":"John Smith","group":["/Group/sw"]}""");

        // named "John Johnson" at this moment
        assertEquals(read(client, "User", "john", ZonedDateTime.now()), """
                {"id":"john","name":"John Johnson","group":["/Group/sw"]}""");

        // still named "John Johnson" a month from now
        assertEquals(read(client, "User", "john", ZonedDateTime.now().plusMonths(1)), """
                {"id":"john","name":"John Johnson","group":["/Group/sw"]}""");

        // named "Jack Johnson" two years from now
        assertEquals(read(client, "User", "john", ZonedDateTime.now().plusYears(2)), """
                {"id":"john","name":"Jack Johnson","group":["/Group/sw"]}""");

        application.stop();
    }

    private void checkNotFound(StingrayHttpClient client, String entity, String resourceId, ZonedDateTime timestamp) {
        client.get()
                .path("/ns/" + entity + "/" + resourceId)
                .query("timestamp", timestamp.toString())
                .path("/ns/User/john")
                .execute()
                .hasStatusCode(404);
    }

    private String read(StingrayHttpClient client, String entity, String resourceId, ZonedDateTime timestamp) {
        String document = client.get()
                .path("/ns/" + entity + "/" + resourceId)
                .query("timestamp", timestamp.toString())
                .execute()
                .hasStatusCode(200)
                .contentAsString();
        return document;
    }

    private void write(StingrayHttpClient client, String entity, String resourceId, ZonedDateTime timestamp, String document) {
        client.put()
                .path("/ns/" + entity + "/" + resourceId)
                .query("timestamp", timestamp.toString())
                .bodyJson(document)
                .execute()
                .isSuccessful();
    }
}
