package com.exoreaction.xorcery.tbv.neo4j.opencypherdsl;

import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.neo4j.cypherdsl.parser.CypherParser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class GenericCypherDslQueryTransformerTest {

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
    public void thatGenericCypherTransformerCanCopy() {
        Statement statement = CypherParser.parse(QUERY);

        GenericCypherDslQueryTransformer copier = new GenericCypherDslQueryTransformer(true);
        statement.accept(copier);
        Statement statementCopy = (Statement) copier.getOutput();

        Renderer renderer = Renderer.getRenderer(Configuration.prettyPrinting());

        String renderedOriginal = renderer.render(statement);
        String renderedCopy = renderer.render(statementCopy);

        assertEquals(renderedCopy, renderedOriginal);
    }
}