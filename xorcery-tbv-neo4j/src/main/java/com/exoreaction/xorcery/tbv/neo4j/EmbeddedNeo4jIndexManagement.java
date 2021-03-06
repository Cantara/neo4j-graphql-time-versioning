package com.exoreaction.xorcery.tbv.neo4j;

import org.neo4j.graphdb.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class EmbeddedNeo4jIndexManagement {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedNeo4jIndexManagement.class);

    private final List<Index> wantedIndexes;
    private final boolean dropExisting;

    static class Index {
        final String label;
        final List<String> properties;
        final boolean uniqueConstraint;

        Index(String label, List<String> properties, boolean uniqueConstraint) {
            this.label = label;
            this.properties = new ArrayList<>(properties);
            this.uniqueConstraint = uniqueConstraint;
        }

        @Override
        public String toString() {
            return "Index{" +
                    "label='" + label + '\'' +
                    ", properties=" + properties +
                    '}';
        }
    }

    EmbeddedNeo4jIndexManagement(String namespace, Set<String> managedDomains, Map<String, Set<String>> customIndexes, boolean dropExisting) {
        this.dropExisting = dropExisting;
        wantedIndexes = new ArrayList<>();
        for (String managedDomain : managedDomains) {
            wantedIndexes.add(new Index(managedDomain + "_R", List.of("id"), true));
        }
        for (Map.Entry<String, Set<String>> fieldsByEntity : customIndexes.entrySet()) {
            String entity = fieldsByEntity.getKey();
            for (String field : fieldsByEntity.getValue()) {
                wantedIndexes.add(new Index(entity, List.of(field), false));
            }
        }
    }

    void createIdIndices(EmbeddedNeo4jTransaction transaction) {
        // TODO https://github.com/neo4j-contrib/neo4j-apoc-procedures/issues/1703
        // TODO Until issue is fixed, we check which indexes and constraints that already exists and avoid including
        // TODO them in apoc.schema.assert call below. This way we don't delete and recreate existing indexes.

        List<Map<String, Object>> constraints = new ArrayList<>();
        List<Map<String, Object>> indexes = new ArrayList<>();
        {
            for (Index index : wantedIndexes) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                map.put("label", index.label);
                map.put("properties", index.properties);
                if (index.uniqueConstraint) {
                    constraints.add(map);
                } else {
                    indexes.add(map);
                }
            }
        }

        boolean[] indexExists = new boolean[indexes.size()];
        {
            String indexExistsQuery = "UNWIND $indexes as i\nRETURN apoc.schema.node.indexExists(i.label, i.properties) AS e";
            try (Result indexExistsResult = transaction.executeCypher(indexExistsQuery, Map.of("indexes", indexes))) {
                int i = 0;
                while (indexExistsResult.hasNext()) {
                    Map<String, Object> record = indexExistsResult.next();
                    indexExists[i++] = (Boolean) record.get("e");
                }
            }
        }

        boolean[] constraintExists = new boolean[constraints.size()];
        {
            String constraintExistsQuery = "UNWIND $constraints as c\nRETURN apoc.schema.node.constraintExists(c.label, c.properties) AS e";
            try (Result constraintExistsResult = transaction.executeCypher(constraintExistsQuery, Map.of("constraints", constraints))) {
                int i = 0;
                while (constraintExistsResult.hasNext()) {
                    Map<String, Object> record = constraintExistsResult.next();
                    constraintExists[i++] = (Boolean) record.get("e");
                }
            }
        }

        Map<String, Object> indexParam = new LinkedHashMap<>();
        Map<String, Object> constraintParam = new LinkedHashMap<>();
        {
            int c = 0;
            int i = 0;
            for (Index index : wantedIndexes) {
                if (index.uniqueConstraint) {
                    if (!constraintExists[c]) {
                        constraintParam.put(index.label, index.properties);
                    }
                    c++;
                } else {
                    if (!indexExists[i]) {
                        indexParam.put(index.label, index.properties);
                    }
                    i++;
                }
            }
        }

        try (Result result = transaction.executeCypher("CALL apoc.schema.assert($indexParam, $constraintParam, $dropExisting) YIELD label, key, keys, unique, action",
                Map.of("indexParam", indexParam, "constraintParam", constraintParam, "dropExisting", false))) {
            result.forEachRemaining(record -> {
                String label = (String) record.get("label");
                String key = (String) record.get("key");
                List<String> keys = (List<String>) record.get("keys");
                boolean unique = (Boolean) record.get("unique");
                String action = (String) record.get("action");
                if ("KEPT".equalsIgnoreCase(action)) {
                    LOG.debug("Index {} - {} : {}{}", action, label, keys, unique ? " unique" : "");
                } else {
                    LOG.info("Index {} - {} : {}{}", action, label, keys, unique ? " unique" : "");
                }
            });
        }

        // TODO if dropExisting is set, drop unwanted existing indexes and constraints
    }
}
