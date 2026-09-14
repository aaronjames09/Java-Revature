package com.aaronjames.bankofcli.db;

/**
 * Runs a UnitOfWork as a single atomic database transaction: commits if it
 * completes normally, rolls back everything if it throws.
 *
 * The Service layer depends on this interface, not on JDBC transaction
 * mechanics directly - keeps commit/rollback logic in exactly one place
 * instead of repeated in every service method.
 */
public interface TransactionManager {
    <T> T executeInTransaction(UnitOfWork<T> unitOfWork);
}
