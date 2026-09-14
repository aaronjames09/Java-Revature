package com.aaronjames.bankofcli.repository;

import com.aaronjames.bankofcli.exception.DataAccessException;
import com.aaronjames.bankofcli.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Optional;

/**
 * Persistence contract for Account data.
 *
 * Every method takes the Connection to use. The repository never opens,
 * commits, or closes a connection itself - that's owned by whoever is
 * coordinating the unit of work (see TransactionManager). This is what lets
 * the Service layer combine multiple repository calls (e.g. debit one
 * account + credit another + write two audit records) into a single atomic
 * database transaction.
 *
 * The Service layer depends on THIS interface, not on
 * PostgresAccountRepository directly (Dependency Inversion) - swap in a
 * different implementation later without touching business logic.
 */
public interface AccountRepository {

    /**
     * Inserts a new account and returns it with its generated accountId populated.
     * @throws DataAccessException if the insert fails
     */
    Account save(Account account, Connection connection);

    /**
     * @return the account if found, otherwise Optional.empty() - never null.
     * @throws DataAccessException if the query fails
     */
    Optional<Account> findById(Long accountId, Connection connection);

    /**
     * Overwrites the stored balance for the given account.
     * @throws DataAccessException if the update fails or no matching account exists
     */
    void updateBalance(Long accountId, BigDecimal newBalance, Connection connection);
}
