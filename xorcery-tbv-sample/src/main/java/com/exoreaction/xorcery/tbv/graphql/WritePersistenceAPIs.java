package com.exoreaction.xorcery.tbv.graphql;

import com.exoreaction.xorcery.tbv.api.persistence.DocumentKey;
import com.exoreaction.xorcery.tbv.api.persistence.PersistenceDeletePolicy;
import com.exoreaction.xorcery.tbv.api.persistence.Transaction;
import com.exoreaction.xorcery.tbv.api.persistence.batch.Batch;
import com.exoreaction.xorcery.tbv.api.persistence.json.JsonDocument;
import com.exoreaction.xorcery.tbv.api.persistence.reactivex.RxJsonPersistence;
import com.exoreaction.xorcery.tbv.api.specification.Specification;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Create/Overwrite, Delete, Batch
 */
public class WritePersistenceAPIs {

    public static void createOrOverwrite(RxJsonPersistence persistence, Specification specification, SagaInput input) {
        String versionStr = input.versionAsString();
        ZonedDateTime version = ZonedDateTime.parse(versionStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        try (Transaction tx = persistence.createTransaction(false)) {
            persistence.createOrOverwrite(tx, new JsonDocument(new DocumentKey(input.namespace(), input.entity(), input.resourceId(), version), input.data()), specification).blockingAwait();
        }
    }

    public static void markAsDeleted(RxJsonPersistence persistence, Specification specification, SagaInput input) {
        String versionStr = input.versionAsString();
        ZonedDateTime version = ZonedDateTime.parse(versionStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        try (Transaction tx = persistence.createTransaction(false)) {
            persistence.markDocumentDeleted(
                    tx,
                    input.namespace(),
                    input.entity(),
                    input.resourceId(),
                    version,
                    PersistenceDeletePolicy.FAIL_IF_INCOMING_LINKS
            ).blockingAwait();
        }
    }

    public static void batchCreateOrOverwrite(RxJsonPersistence persistence, Specification specification, SagaInput input) {
        Batch batch = new Batch(input.batch());
        if (batch.groups().isEmpty()) {
            return; // avoid creating transaction
        }
        try (Transaction tx = persistence.createTransaction(false)) {
            for (Batch.Group group : batch.groups()) {
                if (Batch.GroupType.DELETE == group.groupType()) {
                    persistence.deleteBatchGroup(
                            tx,
                            (Batch.DeleteGroup) group,
                            input.namespace(),
                            specification
                    ).blockingAwait();
                } else if (Batch.GroupType.PUT == group.groupType()) {
                    persistence.putBatchGroup(
                            tx,
                            (Batch.PutGroup) group,
                            input.namespace(),
                            specification
                    ).blockingAwait();
                }
            }
        }
    }
}
