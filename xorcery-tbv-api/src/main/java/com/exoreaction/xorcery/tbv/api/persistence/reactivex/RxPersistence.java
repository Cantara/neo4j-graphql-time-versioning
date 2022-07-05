package com.exoreaction.xorcery.tbv.api.persistence.reactivex;

import com.exoreaction.xorcery.tbv.api.persistence.PersistenceDeletePolicy;
import com.exoreaction.xorcery.tbv.api.persistence.PersistenceException;
import com.exoreaction.xorcery.tbv.api.persistence.Transaction;
import com.exoreaction.xorcery.tbv.api.persistence.TransactionFactory;
import com.exoreaction.xorcery.tbv.api.persistence.streaming.Fragment;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.time.ZonedDateTime;

public interface RxPersistence {

    TransactionFactory transactionFactory() throws PersistenceException;

    Transaction createTransaction(boolean readOnly) throws PersistenceException;

    Completable createOrOverwrite(
            Transaction tx,
            Flowable<Fragment> fragments
    );

    Flowable<Fragment> read(
            Transaction tx,
            ZonedDateTime snapshot,
            String namespace,
            String entity,
            String id
    );

    Flowable<Fragment> readVersions(
            Transaction tx,
            String namespace,
            String entity,
            String id,
            Range<ZonedDateTime> range
    );

    Completable delete(
            Transaction transaction,
            String namespace,
            String entity,
            String id,
            ZonedDateTime version,
            PersistenceDeletePolicy policy
    );

    Completable deleteAllVersions(
            Transaction transaction,
            String namespace,
            String entity,
            String id,
            PersistenceDeletePolicy policy
    );

    Completable deleteAllEntities(Transaction tx, String namespace, String entity, Iterable<String> paths);

    Completable markDeleted(
            Transaction transaction,
            String namespace,
            String entity,
            String id,
            ZonedDateTime version,
            PersistenceDeletePolicy policy
    );

    Flowable<Fragment> readAll(
            Transaction transaction,
            ZonedDateTime snapshot,
            String namespace,
            String entity,
            Range<String> range
    );

    Flowable<Fragment> find(
            Transaction transaction,
            ZonedDateTime snapshot,
            String namespace,
            String entity,
            String path,
            byte[] value,
            Range<String> range
    );

    Single<Boolean> hasPrevious(
            Transaction tx,
            ZonedDateTime snapshot,
            String namespace,
            String entityName,
            String id
    );

    Single<Boolean> hasNext(
            Transaction tx,
            ZonedDateTime snapshot,
            String namespace,
            String entityName,
            String id
    );

    void close() throws PersistenceException;
}
