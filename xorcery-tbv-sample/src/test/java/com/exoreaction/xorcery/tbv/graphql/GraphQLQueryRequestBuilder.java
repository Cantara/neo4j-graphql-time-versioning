package com.exoreaction.xorcery.tbv.graphql;

import com.exoreaction.xorcery.tbv.neo4j.graphql.TBVGraphQLConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class GraphQLQueryRequestBuilder {

    private final ObjectNode graphqlRequestBody;
    private final ObjectNode variables;

    {
        graphqlRequestBody = TestUtils.mapper.createObjectNode();
        variables = graphqlRequestBody.putObject("variables");
    }

    public GraphQLQueryRequestBuilder withQuery(String graphQLQuery) {
        graphqlRequestBody.put("query", graphQLQuery);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, Object value) {
        setJsonField(variables, key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, JsonNode value) {
        variables.set(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, Short value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, short value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, Integer value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, int value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, Long value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, long value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, Float value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, float value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, Double value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, double value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, BigDecimal value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, BigInteger value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, String value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, Boolean value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, boolean value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder addParam(String key, byte[] value) {
        variables.put(key, value);
        return this;
    }

    public GraphQLQueryRequestBuilder withTimeVersion(ZonedDateTime timeVersion) {
        variables.put(TBVGraphQLConstants.VARIABLE_IDENTIFIER_TIME_BASED_VERSION, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(timeVersion));
        return this;
    }

    public JsonNode build() {
        return graphqlRequestBody;
    }

    private static void setJsonField(ObjectNode variables, String key, Object value) {
        if (value instanceof JsonNode v) {
            variables.set(key, v);
        } else if (value instanceof Short v) {
            variables.put(key, v);
        } else if (value instanceof Integer v) {
            variables.put(key, v);
        } else if (value instanceof Long v) {
            variables.put(key, v);
        } else if (value instanceof Float v) {
            variables.put(key, v);
        } else if (value instanceof Double v) {
            variables.put(key, v);
        } else if (value instanceof BigDecimal v) {
            variables.put(key, v);
        } else if (value instanceof BigInteger v) {
            variables.put(key, v);
        } else if (value instanceof String v) {
            variables.put(key, v);
        } else if (value instanceof Boolean v) {
            variables.put(key, v);
        } else if (value instanceof byte[] v) {
            variables.put(key, v);
        } else {
            throw new IllegalArgumentException("Unsupported variable type: " + value.getClass().getName());
        }
    }
}
