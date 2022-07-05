package com.exoreaction.xorcery.tbv.neo4j.apoc.path;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;

public class TBVConstants {
    static final RelationshipType RELATIONSHIP_TYPE_VERSION_OF = RelationshipType.withName("VERSION_OF");
    public static final Label LABEL_RESOURCE = Label.label("RESOURCE");
    public static final Label LABEL_INSTANCE = Label.label("INSTANCE");
    public static final Label LABEL_EMBEDDED = Label.label("EMBEDDED");
    static final Iterable<Relationship> EMPTY = Collections.emptyList();
}
