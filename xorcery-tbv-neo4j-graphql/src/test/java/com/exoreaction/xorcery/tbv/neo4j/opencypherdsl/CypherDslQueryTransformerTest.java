package com.exoreaction.xorcery.tbv.neo4j.opencypherdsl;

import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.ResultStatement;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.neo4j.cypherdsl.parser.CypherParser;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.neo4j.cypherdsl.core.Cypher.anyNode;
import static org.neo4j.cypherdsl.core.Cypher.literalOf;
import static org.neo4j.cypherdsl.core.Cypher.match;
import static org.neo4j.cypherdsl.core.Cypher.name;
import static org.neo4j.cypherdsl.core.Cypher.node;
import static org.neo4j.cypherdsl.core.Cypher.parameter;
import static org.neo4j.cypherdsl.core.Cypher.with;

public class CypherDslQueryTransformerTest {

    public static final String QUERY = """
            MATCH (user:User)
            WHERE user.id = $userId
            CALL {
              WITH user
              CALL {
                WITH user
                WITH user AS this
                MATCH (this)-[:group]->(:RESOURCE)-[v:VERSION]->(n) WHERE v.from <= ver AND coalesce(ver < v.to, true) RETURN n AS userGroup
              }
              RETURN collect(userGroup {
                .name
              }) AS userGroup
            }
            RETURN user {
              .name,
              group: userGroup
            } AS user
            """;

    @Test
    public void thatUsingDslToCreateTestQueryWorks() {
        ResultStatement statement = match(node("User").named("user"))
                .where(name("user").property("id").isEqualTo(parameter("userId")))
                .call(with("user")
                        .call(with("user")
                                .with(name("user").as("this"))
                                .match(anyNode("this")
                                        .relationshipTo(node("RESOURCE"), "group")
                                        .relationshipTo(anyNode("n"), "VERSION").named("v")
                                )
                                .where(name("v").property("from").lte(name("ver"))
                                        .and(Functions.coalesce(name("ver").lt(name("v").property("to")), Cypher.literalTrue()).asCondition())
                                )
                                .returning(name("n").as("userGroup"))
                                .build())
                        .returning(Functions.collect(name("userGroup").project("name")).as("userGroup"))
                        .build())
                .returning(name("user").project("name", "group", name("userGroup")).as("user"))
                .build();

        Renderer prettyRenderer = Renderer.getRenderer(Configuration.prettyPrinting());

        String actual = prettyRenderer.render(statement);

        Statement expectedStatement = CypherParser.parse(QUERY);
        String expected = prettyRenderer.render(expectedStatement);

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void thatUsingDslToCreateProcedureCallWithArgumentsAndYieldReturningQueryWorks() {
        ResultStatement statement = with(name("a"), name("b"))
                .with(name("a").as("this"), name("b").as("that"))
                .call("apoc.create")
                .withArgs(name("this"), literalOf("some-string"))
                .yield(name("path").as("p"))
                .with(name("p").as("n"))
                .returning("n")
                .build();

        Renderer prettyRenderer = Renderer.getRenderer(Configuration.prettyPrinting());

        String actual = prettyRenderer.render(statement);

        Assert.assertEquals(actual, """
                WITH a, b
                WITH a AS this, b AS that CALL apoc.create(this, 'some-string') YIELD path AS p
                WITH p AS n
                RETURN n""");
    }
}