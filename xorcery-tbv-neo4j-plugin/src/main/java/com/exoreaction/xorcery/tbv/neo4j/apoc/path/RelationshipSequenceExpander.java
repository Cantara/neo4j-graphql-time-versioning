package com.exoreaction.xorcery.tbv.neo4j.apoc.path;

import apoc.path.RelationshipTypeAndDirections;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.Iterables;
import org.neo4j.internal.helpers.collection.Iterators;
import org.neo4j.internal.helpers.collection.NestingIterator;
import org.neo4j.internal.helpers.collection.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An expander for repeating sequences of relationships. The sequence provided should be a string consisting of
 * relationship type/direction patterns (exactly the same as the `relationshipFilter`), separated by commas.
 * Each comma-separated pattern represents the relationships that will be expanded with each step of expansion, which
 * repeats indefinitely (unless otherwise stopped by `maxLevel`, `limit`, or terminator filtering from the other expander config options).
 * The exception is if `beginSequenceAtStart` is false. This indicates that the sequence should not begin from the start node,
 * but from one node distant. In this case, we may still need a restriction on the relationship used to reach the start node
 * of the sequence, so when `beginSequenceAtStart` is false, then the first relationship step in the sequence given will not
 * actually be used as part of the sequence, but will only be used once to reach the starting node of the sequence.
 * The remaining relationship steps will be used as the repeating relationship sequence.
 */
public class RelationshipSequenceExpander implements PathExpander {
    static final Iterable<Relationship> EMPTY = Collections.emptyList();
    private final List<List<Pair<RelationshipType, Direction>>> relSequences = new ArrayList<>();
    private final long snapshot;
    private List<Pair<RelationshipType, Direction>> initialRels = null;


    public RelationshipSequenceExpander(long snapshot, String relSequenceString, boolean beginSequenceAtStart) {
        this.snapshot = snapshot;
        int index = 0;

        if (relSequenceString.isEmpty()) {
            List<Pair<RelationshipType, Direction>> stepRels = new ArrayList<>();
            stepRels.add(Pair.of(null, Direction.BOTH));
            relSequences.add(stepRels);
        } else {
            for (String sequenceStep : relSequenceString.split(",")) {
                sequenceStep = sequenceStep.trim();
                Iterable<Pair<RelationshipType, Direction>> relDirIterable = RelationshipTypeAndDirections.parse(sequenceStep);

                List<Pair<RelationshipType, Direction>> stepRels = new ArrayList<>();
                for (Pair<RelationshipType, Direction> pair : relDirIterable) {
                    stepRels.add(pair);
                }

                if (!beginSequenceAtStart && index == 0) {
                    initialRels = stepRels;
                } else {
                    relSequences.add(stepRels);
                }

                index++;
            }
        }
    }

    public RelationshipSequenceExpander(long snapshot, List<String> relSequenceList, boolean beginSequenceAtStart) {
        this.snapshot = snapshot;
        int index = 0;

        for (String sequenceStep : relSequenceList) {
            sequenceStep = sequenceStep.trim();
            Iterable<Pair<RelationshipType, Direction>> relDirIterable = RelationshipTypeAndDirections.parse(sequenceStep);

            List<Pair<RelationshipType, Direction>> stepRels = new ArrayList<>();
            for (Pair<RelationshipType, Direction> pair : relDirIterable) {
                stepRels.add(pair);
            }

            if (!beginSequenceAtStart && index == 0) {
                initialRels = stepRels;
            } else {
                relSequences.add(stepRels);
            }

            index++;
        }
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState state) {
        final Node node = path.endNode();
        int depth = TBVEvaluators.computePathLength(path);
        List<Pair<RelationshipType, Direction>> stepRels;
        if (depth == 0 && initialRels != null) {
            stepRels = initialRels;
        } else {
            stepRels = relSequences.get((initialRels == null ? depth : depth - 1) % relSequences.size());
        }

        if (node.hasLabel(TBVConstants.LABEL_RESOURCE)) {
            /*
             * Last node in path is a RESOURCE node
             */
            List<Relationship> excludes = new ArrayList<>();
            List<Relationship> relationshipsToExpand = new ArrayList<>();
            Relationship lastRelationship = path.lastRelationship();
            if (lastRelationship != null && !lastRelationship.isType(TBVConstants.RELATIONSHIP_TYPE_VERSION_OF)) {
                Relationship resolvedVersionRelationship = resolveTimeBaseVersioningRelationship(node, Direction.INCOMING);
                if (resolvedVersionRelationship == null) {
                    return Iterables.empty(); // do not expand any relationships if resource does not have a valid instance
                }
                relationshipsToExpand.add(resolvedVersionRelationship);
                excludes.add(lastRelationship);
            }
            relationshipsToExpand.addAll(Iterators.asList(
                    new NestingIterator<Relationship, Pair<RelationshipType, Direction>>(
                            stepRels.iterator()) {
                        @Override
                        protected Iterator<Relationship> createNestedIterator(
                                Pair<RelationshipType, Direction> entry) {
                            Direction dir = entry.other();
                            if (dir == Direction.OUTGOING) {
                                return EMPTY.iterator();
                            }
                            RelationshipType type = entry.first();
                            if (type != null) {
                                return filterValidIncomingLinks(node.getRelationships(Direction.INCOMING, type), excludes).iterator();
                            }
                            RelationshipType[] types = getRelationshipTypesExceptVersionOf(node);
                            return filterValidIncomingLinks(node.getRelationships(Direction.INCOMING, types), excludes).iterator();
                        }
                    }));
            return relationshipsToExpand;
        }

        if (node.hasLabel(TBVConstants.LABEL_INSTANCE)) {
            /*
             * Last node in path is an INSTANCE node
             */
            List<Relationship> relationshipsToExpand = new ArrayList<>();
            if (path.lastRelationship() == null || !path.lastRelationship().isType(TBVConstants.RELATIONSHIP_TYPE_VERSION_OF)) {
                // the resource of this instance must be expanded
                Relationship validVersionOf = resolveTimeBaseVersioningRelationship(node, Direction.OUTGOING);
                if (validVersionOf == null) {
                    return Iterables.empty(); // invalid instance, this should really not happen
                }
                relationshipsToExpand.add(validVersionOf);
            }
            relationshipsToExpand.addAll(Iterators.asList(
                    new NestingIterator<Relationship, Pair<RelationshipType, Direction>>(
                            stepRels.iterator()) {
                        @Override
                        protected Iterator<Relationship> createNestedIterator(
                                Pair<RelationshipType, Direction> entry) {
                            Direction dir = entry.other();
                            if (dir == Direction.INCOMING) {
                                return EMPTY.iterator();
                            }
                            RelationshipType type = entry.first();
                            if (type != null) {
                                return node.getRelationships(Direction.OUTGOING, type).iterator();
                            }
                            RelationshipType[] types = getRelationshipTypesExceptVersionOf(node);
                            return node.getRelationships(Direction.OUTGOING, types).iterator();
                        }
                    }));
            return relationshipsToExpand;
        }

        if (node.hasLabel(TBVConstants.LABEL_EMBEDDED)) {
            /*
             * Last node in path is an EMBEDDED node
             */
            List<Relationship> relationshipsToExpand = new ArrayList<>();
            if (path.lastRelationship().getEndNode().equals(node)) {
                relationshipsToExpand.addAll(Iterators.asList(
                        new NestingIterator<Relationship, Pair<RelationshipType, Direction>>(
                                stepRels.iterator()) {
                            @Override
                            protected Iterator<Relationship> createNestedIterator(
                                    Pair<RelationshipType, Direction> entry) {
                                Direction dir = entry.other();
                                if (dir == Direction.INCOMING) {
                                    return EMPTY.iterator();
                                }
                                RelationshipType type = entry.first();
                                if (type != null) {
                                    return node.getRelationships(Direction.OUTGOING, type).iterator();
                                }
                                return node.getRelationships(Direction.OUTGOING).iterator();
                            }
                        }));
            } else {
                // path.lastRelationship().getStartNode().equals(node) == true
                {
                    Iterator<Relationship> iterator = node.getRelationships(Direction.INCOMING).iterator();
                    if (iterator.hasNext()) {
                        relationshipsToExpand.add(iterator.next());
                    }
                    if (iterator instanceof ResourceIterator) {
                        ((ResourceIterator<Relationship>) iterator).close();
                    }
                }
                relationshipsToExpand.addAll(Iterators.asList(
                        new NestingIterator<Relationship, Pair<RelationshipType, Direction>>(
                                stepRels.iterator()) {
                            @Override
                            protected Iterator<Relationship> createNestedIterator(
                                    Pair<RelationshipType, Direction> entry) {
                                Direction dir = entry.other();
                                if (dir == Direction.INCOMING) {
                                    return EMPTY.iterator();
                                }
                                RelationshipType type = entry.first();
                                if (type != null) {
                                    if (!path.lastRelationship().isType(type)) {
                                        return node.getRelationships(Direction.OUTGOING, type).iterator();
                                    }
                                    List<Relationship> list = new LinkedList<>();
                                    for (Relationship relationship : node.getRelationships(Direction.OUTGOING, type)) {
                                        if (relationship.equals(path.lastRelationship())) {
                                            continue; // exclude
                                        }
                                        list.add(relationship);
                                    }
                                    return list.iterator();
                                }
                                return node.getRelationships(Direction.OUTGOING).iterator();
                            }
                        }));
            }
            return relationshipsToExpand;
        }
        throw new IllegalStateException("Graph data is not xorcery tbv compatible");
    }

    private RelationshipType[] getRelationshipTypesExceptVersionOf(Node node) {
        List<RelationshipType> typeList = new ArrayList<>();
        for (RelationshipType rt : node.getRelationshipTypes()) {
            if (!TBVConstants.RELATIONSHIP_TYPE_VERSION_OF.equals(rt)) {
                typeList.add(rt);
            }
        }
        RelationshipType[] typeArray = typeList.toArray(new RelationshipType[0]);
        return typeArray;
    }

    private Iterable<Relationship> filterValidIncomingLinks(Iterable<Relationship> relationships, List<Relationship> excludes) {
        List<Relationship> list = new LinkedList<>();
        for (Relationship relationship : relationships) {
            if (excludes.stream()
                    .filter(relationship::equals)
                    .map(r -> Boolean.TRUE)
                    .findAny()
                    .orElse(Boolean.FALSE)) {
                continue; // exclude relationship
            }
            Node node = relationship.getStartNode();
            while (node.hasLabel(TBVConstants.LABEL_EMBEDDED)) {
                Iterator<Relationship> iterator = node.getRelationships(Direction.INCOMING).iterator();
                if (iterator.hasNext()) {
                    node = iterator.next().getStartNode();
                }
                if (iterator instanceof ResourceIterator) {
                    ((ResourceIterator<Relationship>) iterator).close();
                }
            }
            // node.hasLabel(LABEL_INSTANCE) == true
            Iterator<Relationship> iterator = node.getRelationships(Direction.OUTGOING, TBVConstants.RELATIONSHIP_TYPE_VERSION_OF).iterator();
            Relationship versionOf = null;
            if (iterator.hasNext()) {
                versionOf = iterator.next();
            }
            if (iterator instanceof ResourceIterator) {
                ((ResourceIterator<Relationship>) iterator).close();
            }

            if (versionOf != null && isVersionValid(versionOf)) {
                list.add(relationship);
            }
        }
        return list;
    }

    private boolean isVersionValid(Relationship versionOf) {
        long from = (Long) versionOf.getProperty("from");
        if (from >snapshot) {
            return false;
        }
        if (!versionOf.hasProperty("to")) {
            return true;
        }
        long to = (Long) versionOf.getProperty("to");
        if (to > snapshot) {
            return true;
        }
        return false;
    }

    private Relationship resolveTimeBaseVersioningRelationship(Node node, Direction direction) {
        Iterable<Relationship> versionOfRelations = node.getRelationships(direction, TBVConstants.RELATIONSHIP_TYPE_VERSION_OF);
        Iterator<Relationship> iterator = versionOfRelations.iterator();
        while (iterator.hasNext()) {
            Relationship relationship = iterator.next();
            if (isVersionValid(relationship)) {
                if (iterator instanceof ResourceIterator) {
                    ((ResourceIterator<Relationship>) iterator).close();
                }
                return relationship;
            }
        }
        return null;
    }

    @Override
    public PathExpander reverse() {
        throw new RuntimeException("Not implemented");
    }
}
