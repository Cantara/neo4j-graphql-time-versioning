package com.exoreaction.xorcery.tbv.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.testng.Assert.assertEquals;

public class TimeBasedVersioningTest {

    static UndertowApplication application;

    static TBVClient client;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String neo4jEmbeddedDataFolder = "target/neo4j/sampledata";

        TestUtils.deleteFolderAndContents(neo4jEmbeddedDataFolder);

        application = UndertowApplication.initializeUndertowApplication(
                9090,
                "src/test/resources/graphqlschemas/accesscontrol.graphql",
                neo4jEmbeddedDataFolder,
                "/ns"
        );

        application.start();

        client = new TBVClient("/ns", application.getHost(), application.getPort());
    }

    @AfterClass
    public static void afterClass() {
        application.stop();
    }

    @Test
    public void thatBasicReadThenWriteWorksWithTimeVersioning() {
        TestUtils.deleteAll(application, "User", "Group");
        writeJohnsHistory();

        // did not exist a month ago
        client.checkNotFound("User", "john", ZonedDateTime.now().minusMonths(1));

        // named "John Smith" two hours ago
        assertEquals(client.read("User", "john", ZonedDateTime.now().minusHours(2)), """
                {"id":"john","name":"John Smith","group":["/Group/sw"]}""");

        // named "John Johnson" at this moment
        assertEquals(client.read("User", "john", ZonedDateTime.now()), """
                {"id":"john","name":"John Johnson","group":["/Group/sw"]}""");

        // still named "John Johnson" a month from now
        assertEquals(client.read("User", "john", ZonedDateTime.now().plusMonths(1)), """
                {"id":"john","name":"John Johnson","group":["/Group/sw"]}""");

        // named "Jack Johnson" two years from now
        assertEquals(client.read("User", "john", ZonedDateTime.now().plusYears(2)), """
                {"id":"john","name":"Jack Johnson","group":["/Group/sw"]}""");
    }

    @Test
    public void thatGraphQLQueryWorksWithTimeBasedVersioning() {
        TestUtils.deleteAll(application, "User", "Group");

        writeJohnsHistory();

        JsonNode twoDaysAgoResponse = client.sendGraphQLQuery(builder -> builder
                .withQuery("""
                        query ($userId: String) {
                           user(filter: {id: $userId}) {
                             name
                           }
                         }""")
                .addParam("userId", "john")
                .withTimeVersion(ZonedDateTime.now().minusDays(2))
        );

        assertEquals(twoDaysAgoResponse.get("data").get(0).get("user").get("name").asText(), "John Smith");

        JsonNode nowResponse = client.sendGraphQLQuery(builder -> builder
                .withQuery("""
                        query ($userId: String) {
                           user(filter: {id: $userId}) {
                             name
                           }
                         }""")
                .addParam("userId", "john")
                .withTimeVersion(ZonedDateTime.now())
        );

        assertEquals(nowResponse.get("data").get(0).get("user").get("name").asText(), "John Johnson");

        JsonNode sixMonthsIntoFutureResponse = client.sendGraphQLQuery(builder -> builder
                .withQuery("""
                        query ($userId: String) {
                           user(filter: {id: $userId}) {
                             name
                           }
                         }""")
                .addParam("userId", "john")
                .withTimeVersion(ZonedDateTime.now().plusMonths(6))
        );

        assertEquals(sixMonthsIntoFutureResponse.get("data").get(0).get("user").get("name").asText(), "Jack Johnson");
    }

    private void writeJohnsHistory() {
        client.write("User", "john", ZonedDateTime.now().minusDays(10), """
                {
                    "id" : "john",
                    "name" : "John Smith",
                    "group" : ["/Group/sw"]
                }
                """);

        client.write("Group", "RnD", ZonedDateTime.now().minusDays(5), """
                {
                    "id" : "RnD",
                    "name" : "Research & Development"
                }""");

        client.write("Group", "sw", ZonedDateTime.now().minusDays(4), """
                {
                    "id" : "sw",
                    "name" : "Software",
                    "parent": ["/Group/RnD"]
                }
                """);

        client.write("Group", "pm", ZonedDateTime.now().minusDays(4), """
                {
                    "id" : "pm",
                    "name" : "Product Managment",
                    "parent": ["/Group/RnD"]
                }
                """);

        client.write("Group", "RnD", ZonedDateTime.now().minusDays(3), """
                {
                    "id" : "RnD",
                    "name" : "Research & Donuts"
                }
                """);

        client.write("User", "john/name", ZonedDateTime.now().minusMinutes(5), """
                "John Johnson"
                """);

        // schedule a name-change in the future, 3 months from now
        client.write("User", "john/name", ZonedDateTime.now().plusMonths(3), """
                "Jack Johnson"
                """);
    }
}
