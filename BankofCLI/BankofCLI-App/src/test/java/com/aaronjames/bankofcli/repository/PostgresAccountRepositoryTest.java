package com.aaronjames.bankofcli.repository;

import com.aaronjames.bankofcli.config.ConfigLoader;
import com.aaronjames.bankofcli.config.DatabaseConfig;
import com.aaronjames.bankofcli.db.ConnectionProvider;
import com.aaronjames.bankofcli.db.PostgresConnectionProvider;
import com.aaronjames.bankofcli.exception.DataAccessException;
import com.aaronjames.bankofcli.model.Account;
import com.aaronjames.bankofcli.util.PinHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests - run against a REAL local PostgreSQL test database.
 * See SETUP_LAYER1.md for one-time setup (create bankofcli_test, load
 * schema.sql, add application-test.properties with real credentials).
 */
class PostgresAccountRepositoryTest {

    private Connection connection;
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConfig config = ConfigLoader.loadDatabaseConfig("application-test.properties");
        ConnectionProvider connectionProvider = new PostgresConnectionProvider(config);
        connection = connectionProvider.getConnection();
        accountRepository = new PostgresAccountRepository();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE transactions, accounts RESTART IDENTITY CASCADE");
        }
        connection.close();
    }

    private Account newTestAccount(String holderName, BigDecimal balance) {
        String salt = PinHasher.generateSalt();
        String hash = PinHasher.hash("1234", salt);
        return new Account(holderName, hash, salt, balance);
    }

    // ---------- save() ----------

    @Test
    void save_positiveCase_persistsAccountAndAssignsId() {
        Account account = newTestAccount("Aaron James", new BigDecimal("100.00"));

        Account saved = accountRepository.save(account, connection);

        assertNotNull(saved.getAccountId(), "Saved account should have a generated ID");
        Optional<Account> reloaded = accountRepository.findById(saved.getAccountId(), connection);
        assertTrue(reloaded.isPresent());
        assertEquals("Aaron James", reloaded.get().getAccountHolder());
        assertEquals(0, new BigDecimal("100.00").compareTo(reloaded.get().getBalance()));
    }

    @Test
    void save_negativeCase_nullAccountThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> accountRepository.save(null, connection));
    }

    // ---------- findById() ----------

    @Test
    void findById_positiveCase_returnsMatchingAccount() {
        Account saved = accountRepository.save(newTestAccount("Jane Doe", new BigDecimal("50.00")), connection);

        Optional<Account> found = accountRepository.findById(saved.getAccountId(), connection);

        assertTrue(found.isPresent());
        assertEquals(saved.getAccountId(), found.get().getAccountId());
    }

    @Test
    void findById_negativeCase_unknownIdReturnsEmpty() {
        Optional<Account> found = accountRepository.findById(999_999L, connection);

        assertTrue(found.isEmpty(), "Looking up a non-existent account should return empty, not throw");
    }

    // ---------- updateBalance() ----------

    @Test
    void updateBalance_positiveCase_changesStoredBalance() {
        Account saved = accountRepository.save(newTestAccount("Sam Lee", new BigDecimal("10.00")), connection);

        accountRepository.updateBalance(saved.getAccountId(), new BigDecimal("250.00"), connection);

        Optional<Account> reloaded = accountRepository.findById(saved.getAccountId(), connection);
        assertTrue(reloaded.isPresent());
        assertEquals(0, new BigDecimal("250.00").compareTo(reloaded.get().getBalance()));
    }

    @Test
    void updateBalance_negativeCase_unknownAccountThrowsDataAccessException() {
        assertThrows(DataAccessException.class,
                () -> accountRepository.updateBalance(999_999L, new BigDecimal("10.00"), connection));
    }
}
