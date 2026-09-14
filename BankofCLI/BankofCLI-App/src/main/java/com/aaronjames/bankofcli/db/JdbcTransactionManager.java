package com.aaronjames.bankofcli.db;

import com.aaronjames.bankofcli.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Opens one Connection per call, turns off auto-commit, runs the given
 * UnitOfWork, and either commits (success) or rolls back (any exception).
 *
 * This is the ONLY place in the whole application that calls commit() or
 * rollback() - every repository method just executes SQL against whatever
 * connection it's handed.
 */
public class JdbcTransactionManager implements TransactionManager {

    private static final Logger LOGGER = Logger.getLogger(JdbcTransactionManager.class.getName());

    private final ConnectionProvider connectionProvider;

    public JdbcTransactionManager(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public <T> T executeInTransaction(UnitOfWork<T> unitOfWork) {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);

            try {
                T result = unitOfWork.execute(connection);
                connection.commit();
                return result;

            } catch (RuntimeException e) {
                rollbackQuietly(connection);
                throw e;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to open or configure a transactional connection", e);
            throw new DataAccessException("Could not start database transaction", e);
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
            LOGGER.info("Transaction rolled back");
        } catch (SQLException rollbackFailure) {
            // The original business exception is more useful to the caller than this one,
            // so we log it here rather than letting it mask the real failure.
            LOGGER.log(Level.SEVERE, "Rollback itself failed", rollbackFailure);
        }
    }
}
