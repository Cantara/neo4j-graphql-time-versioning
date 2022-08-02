package com.exoreaction.xorcery.tbv.graphql;

import com.exoreaction.xorcery.tbv.api.persistence.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Completable;

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

    public static Completable deleteAll(UndertowApplication application, String namespace, String... entities) {
        Completable[] completables = new Completable[entities.length];
        Transaction tx = application.getPersistence().createTransaction(false);
        try {
            for (int i = 0; i < entities.length; i++) {
                completables[i] = application.getPersistence().deleteAllEntities(tx, namespace, entities[i], application.getSpecification());
            }
        } catch (Throwable t) {
            tx.close();
            throw t;
        }
        return Completable.mergeArray(completables)
                .doOnTerminate(tx::close);
    }
}
