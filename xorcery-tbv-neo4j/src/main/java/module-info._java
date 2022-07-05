import com.exoreaction.xorcery.tbv.api.persistence.PersistenceInitializer;
import com.exoreaction.xorcery.tbv.neo4j.EmbeddedNeo4jInitializer;
import com.exoreaction.xorcery.tbv.neo4j.Neo4jInitializer;

module com.exoreaction.xorcery.tbv.neo4j {
    requires com.exoreaction.xorcery.tbv.api;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.neo4j.driver;
    requires java.logging;
    requires jul.to.slf4j;
    requires io.reactivex.rxjava2;
    requires org.reactivestreams;
    requires graphql.java;
    requires org.antlr.antlr4.runtime;
    requires org.slf4j;
    requires jackson.dataformat.msgpack;
    requires org.neo4j.dbms;
    requires org.neo4j.graphdb;
    requires org.neo4j.community;

    provides PersistenceInitializer with Neo4jInitializer, EmbeddedNeo4jInitializer;
}
