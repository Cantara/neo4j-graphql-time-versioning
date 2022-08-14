package com.exoreaction.xorcery.tbv.neo4j.apoc.path;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.PathEvaluator;

public abstract class TBVEvaluators {

    /**
     * Returns an {@link Evaluator} which includes positions down to {@code depth}
     * and prunes everything deeper than that.
     *
     * @param depth   the max depth to traverse to.
     * @param <STATE> the type of the state object.
     * @return Returns an {@link Evaluator} which includes positions down to
     * {@code depth} and prunes everything deeper than that.
     */
    public static <STATE> PathEvaluator<STATE> toDepth(final int depth) {
        return new PathEvaluator.Adapter() {
            @Override
            public Evaluation evaluate(Path path, BranchState state) {
                int pathLength = computePathLength(path);
                Evaluation evaluation = Evaluation.of(pathLength <= depth, pathLength < depth);
                return evaluation;
            }
        };
    }

    /**
     * Returns an {@link Evaluator} which only includes positions from {@code depth}
     * and deeper and never prunes anything.
     *
     * @param depth   the depth to start include positions from.
     * @param <STATE> the type of the state object.
     * @return Returns an {@link Evaluator} which only includes positions from
     * {@code depth} and deeper and never prunes anything.
     */
    public static <STATE> PathEvaluator<STATE> fromDepth(final int depth) {
        return new PathEvaluator.Adapter() {
            @Override
            public Evaluation evaluate(Path path, BranchState state) {
                int pathLength = computePathLength(path);
                Evaluation evaluation = Evaluation.ofIncludes(pathLength >= depth);
                return evaluation;
            }
        };
    }

    static int computePathLength(Path path) {
        int pathLength = path.length();
        for (Relationship r : path.relationships()) {
            if (r.isType(TBVConstants.RELATIONSHIP_TYPE_VERSION_OF)) {
                pathLength--;
            }
        }
        if (path.endNode().hasLabel(TBVConstants.LABEL_RESOURCE)) {
            if (path.lastRelationship() != null && !path.lastRelationship().isType(TBVConstants.RELATIONSHIP_TYPE_VERSION_OF)) {
                pathLength--;
            }
        }
        return pathLength;
    }
}
