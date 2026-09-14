package com.aaronjames.bankofcli.db;

import java.sql.Connection;

/**
 * A block of repository calls that must all succeed or all be rolled back
 * together, e.g. "debit account A, credit account B, write two audit rows."
 *
 * Named to avoid confusion with the banking domain's own "Transaction"
 * concept (a deposit/withdraw/transfer record) - this is purely a database
 * transaction boundary.
 */
@FunctionalInterface
public interface UnitOfWork<T> {
    T execute(Connection connection);
}
