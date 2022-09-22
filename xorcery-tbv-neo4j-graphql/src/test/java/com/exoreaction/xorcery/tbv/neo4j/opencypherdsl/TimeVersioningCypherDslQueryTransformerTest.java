package com.exoreaction.xorcery.tbv.neo4j.opencypherdsl;

import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.neo4j.cypherdsl.parser.CypherParser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TimeVersioningCypherDslQueryTransformerTest {

    public static final String QUERY = """
            MATCH (user:User)
            WHERE user.id = $userId
            CALL {
            	WITH user
            	CALL {
            		WITH user
            		WITH user AS this, $_tbv AS ver
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
    public void thatTimeVersioningTransformerDoesTransformTopLevelQuery() {
        Statement statement = CypherParser.parse(QUERY);

        TimeVersioningCypherDslQueryTransformer transformer = new TimeVersioningCypherDslQueryTransformer(false);
        statement.accept(transformer);
        Statement transformedStatement = (Statement) transformer.getOutput();

        Renderer renderer = Renderer.getRenderer(Configuration.prettyPrinting());

        String renderedTransformed = renderer.render(transformedStatement);

        assertEquals(renderedTransformed, """
                MATCH (_r:User_R)<-[_v:VERSION_OF]-(user)
                WHERE (_v.from <= $_tbv
                  AND coalesce($_tbv < _v.to, true)
                  AND user.id = $userId)
                CALL {
                  WITH user
                  CALL {
                    WITH user
                    WITH user AS this, $_tbv AS ver
                    MATCH (this)-[:group]->(:RESOURCE)<-[v:VERSION_OF]-(n)
                    WHERE (v.from <= ver
                      AND coalesce(ver < v.to, true))
                    RETURN n AS userGroup
                  }
                  RETURN collect(userGroup {
                    .name
                  }) AS userGroup
                }
                RETURN user {
                  .name,
                  group: userGroup
                } AS user""");
    }


    private static final String QUERY_2 = """
            MATCH (person:Person)
            WHERE person.name = $personName
            CALL {
            	WITH person
            	CALL {
            		WITH person
            		WITH person AS this, $_tbv AS ver
            		MATCH (this)-[:VERSION_OF]->(r)<-[v:VERSION_OF]-(i) WITH i ORDER BY v.from DESC RETURN i AS person_history
            	}
            	RETURN collect(person_history {
            		.name
            	}) AS person_history
            }
            RETURN person {
            	.id,
            	.name,
            	.gender,
            	_history: person_history
            } AS person
            """;

    @Test
    public void thatOrderByAfterWithBeforeReturnInTimeVersionParameterWorks() {
        Statement statement = CypherParser.parse(QUERY_2);

        TimeVersioningCypherDslQueryTransformer transformer = new TimeVersioningCypherDslQueryTransformer(false);
        statement.accept(transformer);
        Statement transformedStatement = (Statement) transformer.getOutput();

        Renderer renderer = Renderer.getRenderer(Configuration.prettyPrinting());

        String renderedTransformed = renderer.render(transformedStatement);

        assertEquals(renderedTransformed, """
                MATCH (_r:Person_R)<-[_v:VERSION_OF]-(person)
                WHERE (_v.from <= $_tbv
                  AND coalesce($_tbv < _v.to, true)
                  AND person.name = $personName)
                CALL {
                  WITH person
                  CALL {
                    WITH person
                    WITH person AS this, $_tbv AS ver
                    MATCH (this)-[:VERSION_OF]->(r)<-[v:VERSION_OF]-(i)
                    WITH i ORDER BY v.from DESC
                    RETURN i AS person_history
                  }
                  RETURN collect(person_history {
                    .name
                  }) AS person_history
                }
                RETURN person {
                  .id,
                  .name,
                  .gender,
                  _history: person_history
                } AS person""");
    }

}