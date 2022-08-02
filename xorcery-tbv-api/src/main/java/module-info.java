module com.exoreaction.xorcery.tbv.api {
    requires io.reactivex.rxjava2;
    requires org.reactivestreams;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.graphqljava;

    exports com.exoreaction.xorcery.tbv.api.persistence;
    exports com.exoreaction.xorcery.tbv.api.persistence.flattened;
    exports com.exoreaction.xorcery.tbv.api.persistence.streaming;
    exports com.exoreaction.xorcery.tbv.api.persistence.json;
    exports com.exoreaction.xorcery.tbv.api.persistence.reactivex;
    exports com.exoreaction.xorcery.tbv.api.specification;
    exports com.exoreaction.xorcery.tbv.api.persistence.batch;
    exports com.exoreaction.xorcery.tbv.api.json;
}
