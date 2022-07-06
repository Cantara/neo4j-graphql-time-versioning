package com.exoreaction.xorcery.tbv.graphql;

import com.exoreaction.xorcery.tbv.api.persistence.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class TestUtils {

    static final ObjectMapper mapper = new ObjectMapper();

    public static void deleteFolderAndContents(String neo4jEmbeddedDataFolder) throws IOException {
        Path path = Path.of(neo4jEmbeddedDataFolder);
        if (!Files.exists(path)) {
            return;
        }
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static void deleteAll(UndertowApplication application, String namespace, String... entities) {
        try (Transaction tx = application.getPersistence().createTransaction(false)) {
            for (String entity : entities) {
                application.getPersistence().deleteAllEntities(tx, namespace, entity, application.getSpecification());
            }
        }
    }
}
