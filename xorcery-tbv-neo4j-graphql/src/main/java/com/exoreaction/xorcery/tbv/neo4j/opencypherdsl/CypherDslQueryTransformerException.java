package com.exoreaction.xorcery.tbv.neo4j.opencypherdsl;

public class CypherDslQueryTransformerException extends RuntimeException {
    public CypherDslQueryTransformerException() {
        super();
    }

    public CypherDslQueryTransformerException(String message) {
        super(message);
    }

    public CypherDslQueryTransformerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CypherDslQueryTransformerException(Throwable cause) {
        super(cause);
    }

    protected CypherDslQueryTransformerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
