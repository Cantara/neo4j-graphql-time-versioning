package com.exoreaction.xorcery.tbv.neo4j;

import com.exoreaction.xorcery.tbv.api.persistence.PersistenceException;
import com.exoreaction.xorcery.tbv.api.persistence.Transaction;
import com.exoreaction.xorcery.tbv.api.persistence.TransactionFactory;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
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
    public Neo4jTransaction createTransaction(boolean readOnly) throws PersistenceException {
        graphDb.beginTx();
        Session session = driver.session(readOnly ? SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build() :
                SessionConfig.builder().withDefaultAccessMode(AccessMode.WRITE).build());
        return new Neo4jTransaction(session, logCypher);
    }

    @Override
    public void close() {
        driver.close();
    }

    <T> T writeTransaction(Function<org.neo4j.driver.Transaction, T> work) {
        boolean committed = false;
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(AccessMode.WRITE).build())) {
            T result = session.writeTransaction(tx -> work.apply(tx));
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

    <T> T readTransaction(Function<org.neo4j.driver.Transaction, T> work) {
        boolean committed = false;
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build())) {
            T result = session.readTransaction(tx -> work.apply(tx));
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

    <T> T readAutoCommit(Function<Session, T> work) {
        boolean committed = false;
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build())) {
            T result = work.apply(session);
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
