package com.exoreaction.xorcery.tbv.neo4j.apoc.path;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class TBVLabelEvaluator implements Evaluator {
    @Override
    public Evaluation evaluate(Path path) {
        Node endNode = path.endNode();
        if (endNode.hasLabel(TBVConstants.LABEL_INSTANCE)) {
            return Evaluation.INCLUDE_AND_CONTINUE;
        }
        if (endNode.hasLabel(TBVConstants.LABEL_EMBEDDED)) {
            return Evaluation.INCLUDE_AND_CONTINUE;
        }
        if (endNode.hasLabel(TBVConstants.LABEL_RESOURCE)) {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
        return Evaluation.EXCLUDE_AND_PRUNE;
    }
}
