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

        client.put()
                .path("/ns/User/john")
                .query("timestamp", ZonedDateTime.now().minusDays(10).toString())
                .bodyJson("""
                        {
                            "id" : "john",
                            "name" : "John Smith",
                            "group" : ["/Group/sw"]
                        }
                        """)
                .execute()
                .hasStatusCode(201);

        client.put()
                .path("/ns/Group/RnD")
                .query("timestamp", ZonedDateTime.now().minusDays(5).toString())
                .bodyJson("""
                        {
                            "id" : "RnD",
                            "name" : "Research & Development"
                        }
                        """)
                .execute()
                .hasStatusCode(201);

        client.put()
                .path("/ns/Group/sw")
                .query("timestamp", ZonedDateTime.now().minusDays(4).toString())
                .bodyJson("""
                        {
                            "id" : "sw",
                            "name" : "Software",
                            "parent": ["/Group/RnD"]
                        }
                        """)
                .execute()
                .hasStatusCode(201);

        client.put()
                .path("/ns/Group/pm")
                .query("timestamp", ZonedDateTime.now().minusDays(4).toString())
                .bodyJson("""
                        {
                            "id" : "pm",
                            "name" : "Product Managment",
                            "parent": ["/Group/RnD"]
                        }
                        """)
                .execute()
                .hasStatusCode(201);

        client.put()
                .path("/ns/Group/RnD")
                .query("timestamp", ZonedDateTime.now().minusDays(3).toString())
                .bodyJson("""
                        {
                            "id" : "RnD",
                            "name" : "Research & Donuts"
                        }
                        """)
                .execute()
                .hasStatusCode(201);

        client.put()
                .path("/ns/User/john/name")
                .query("timestamp", ZonedDateTime.now().minusMinutes(5).toString())
                .bodyJson("""
                        "John Johnson"
                        """)
                .execute()
                .hasStatusCode(200);

        String johnJson = client.get()
                .query("timestamp", ZonedDateTime.now().toString())
                .path("/ns/User/john")
                .execute()
                .hasStatusCode(200)
                .contentAsString();

        assertEquals(johnJson, """
                {"id":"john","name":"John Johnson","group":["/Group/sw"]}""");

        String johnJsonYesterday = client.get()
                .query("timestamp", ZonedDateTime.now().minusHours(2).toString())
                .path("/ns/User/john")
                .execute()
                .hasStatusCode(200)
                .contentAsString();

        assertEquals(johnJsonYesterday, """
                {"id":"john","name":"John Smith","group":["/Group/sw"]}""");

        client.get()
                .query("timestamp", ZonedDateTime.now().minusMonths(1).toString())
                .path("/ns/User/john")
                .execute()
                .hasStatusCode(404); // resource did not exist in databse a month ago

        application.stop();
    }
}
