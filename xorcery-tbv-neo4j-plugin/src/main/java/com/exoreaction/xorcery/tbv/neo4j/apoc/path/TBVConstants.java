package com.exoreaction.xorcery.tbv.neo4j.apoc.path;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

public class TBVConstants {
    static final RelationshipType RELATIONSHIP_TYPE_VERSION = RelationshipType.withName("VERSION");
    public static final Label LABEL_RESOURCE = Label.label("RESOURCE");
    public static final Label LABEL_INSTANCE = Label.label("INSTANCE");
    public static final Label LABEL_EMBEDDED = Label.label("EMBEDDED");
}
