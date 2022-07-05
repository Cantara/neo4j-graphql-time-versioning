package com.exoreaction.xorcery.tbv.neo4j;

import com.exoreaction.xorcery.tbv.api.persistence.PersistenceException;
import com.exoreaction.xorcery.tbv.api.persistence.Transaction;
import com.exoreaction.xorcery.tbv.api.persistence.TransactionFactory;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class EmbeddedNeo4jTransactionFactory implements TransactionFactory {

    final GraphDatabaseService graphDb;
    final boolean logCypher;

    EmbeddedNeo4jTransactionFactory(GraphDatabaseService graphDb, boolean logCypher) {
        this.graphDb = graphDb;
        this.logCypher = logCypher;
    }

    @Override
    public <T> CompletableFuture<T> runAsyncInIsolatedTransaction(Function<? super Transaction, ? extends T> retryable, boolean readOnly) {
        return null;
    }

    @Override
    public EmbeddedNeo4jTransaction createTransaction(boolean readOnly) throws PersistenceException {
        return new EmbeddedNeo4jTransaction(graphDb, logCypher);
    }

    @Override
    public void close() {
        // TODO close database?
    }

    <T> T writeTransaction(Function<org.neo4j.graphdb.Transaction, T> work) {
        boolean committed = false;
        try (org.neo4j.graphdb.Transaction tx = graphDb.beginTx()) {
            T result = work.apply(tx);
            committed = true;
            return result;
        } catch (PersistenceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        } finally {
            if (logCypher) {
                if (committed) {
                    System.out.println("WRITE COMMITED");
                } else {
                    System.out.println("WRITE ROLLED-BACK");
                }
            }
        }
    }

    <T> T readTransaction(Function<org.neo4j.graphdb.Transaction, T> work) {
        boolean committed = false;
        try (org.neo4j.graphdb.Transaction tx = graphDb.beginTx()) {
            T result = work.apply(tx);
            committed = true;
            return result;
        } catch (PersistenceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        } finally {
            if (logCypher) {
                if (committed) {
                    System.out.println("READ COMMITED");
                } else {
                    System.out.println("READ ROLLED-BACK");
                }
            }
        }
    }

    <T> T readAutoCommit(Function<org.neo4j.graphdb.Transaction, T> work) {
        boolean committed = false;
        try (org.neo4j.graphdb.Transaction tx = graphDb.beginTx()) {
            T result = work.apply(tx);
            committed = true;
            return result;
        } catch (PersistenceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        } finally {
            if (logCypher) {
                if (committed) {
                    System.out.println("READ COMMITED");
                } else {
                    System.out.println("READ ROLLED-BACK");
                }
            }
        }
    }
}
