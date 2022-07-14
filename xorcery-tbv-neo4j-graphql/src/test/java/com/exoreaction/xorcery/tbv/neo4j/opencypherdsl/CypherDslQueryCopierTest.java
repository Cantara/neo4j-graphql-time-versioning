package com.exoreaction.xorcery.tbv.neo4j.opencypherdsl;

import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.neo4j.cypherdsl.parser.CypherParser;
import org.testng.annotations.Test;

public class CypherDslQueryCopierTest {

    public static final String QUERY = """
            MATCH (user:User)
            WHERE user.id = $userId
            CALL {
              WITH user
              CALL {
                WITH user
                WITH user AS this
                MATCH (this)-[:group]->(:RESOURCE)<-[v:VERSION_OF]-(n) WHERE v.from <= ver AND coalesce(ver < v.to, true) RETURN n AS userGroup
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
    public void thatSomething() {
        Statement statement = CypherParser.parse(QUERY);

        CypherDslQueryCopier copier = new CypherDslQueryCopier();
        statement.accept(copier);
        Statement statementCopy = copier.getStatementCopy();

        String renderedQuery = Renderer.getRenderer(Configuration.prettyPrinting()).render(statementCopy);
        System.out.println();
        System.out.printf("%s%n", renderedQuery);
    }

}