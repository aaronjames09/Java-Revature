package com.aaronjames.bankofcli.repository;

import com.aaronjames.bankofcli.exception.DataAccessException;
import com.aaronjames.bankofcli.model.TransactionRecord;

import java.sql.Connection;
import java.util.List;

/**
 * Persistence contract for the transaction audit trail.
 * Like AccountRepository, every method takes the Connection to use so it
 * can participate in a larger atomic transaction (see TransactionManager).
 */
public interface TransactionRepository {

    /**
     * Inserts one audit trail row and returns it with its generated ID populated.
     * @throws DataAccessException if the insert fails
     */
    TransactionRecord record(TransactionRecord transactionRecord, Connection connection);

    /**
     * @return up to {@code limit} of the account's most recent transactions, newest first.
     * @throws DataAccessException if the query fails
     */
    List<TransactionRecord> findRecentByAccountId(Long accountId, int limit, Connection connection);
}
