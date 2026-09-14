package com.aaronjames.bankofcli.service;

import com.aaronjames.bankofcli.config.ConfigLoader;
import com.aaronjames.bankofcli.config.DatabaseConfig;
import com.aaronjames.bankofcli.db.ConnectionProvider;
import com.aaronjames.bankofcli.db.JdbcTransactionManager;
import com.aaronjames.bankofcli.db.PostgresConnectionProvider;
import com.aaronjames.bankofcli.db.TransactionManager;
import com.aaronjames.bankofcli.exception.AccountNotFoundException;
import com.aaronjames.bankofcli.exception.InsufficientFundsException;
import com.aaronjames.bankofcli.exception.InvalidAmountException;
import com.aaronjames.bankofcli.exception.InvalidPinException;
import com.aaronjames.bankofcli.model.Account;
import com.aaronjames.bankofcli.repository.AccountRepository;
import com.aaronjames.bankofcli.repository.PostgresAccountRepository;
import com.aaronjames.bankofcli.repository.PostgresTransactionRepository;
import com.aaronjames.bankofcli.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests - run against a REAL local PostgreSQL test database.
 * See SETUP_LAYER1.md for one-time setup.
 */
class AccountServiceImplTest {

    private ConnectionProvider connectionProvider;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        DatabaseConfig config = ConfigLoader.loadDatabaseConfig("application-test.properties");
        connectionProvider = new PostgresConnectionProvider(config);

        TransactionManager transactionManager = new JdbcTransactionManager(connectionProvider);
        AccountRepository accountRepository = new PostgresAccountRepository();
        TransactionRepository transactionRepository = new PostgresTransactionRepository();

        accountService = new AccountServiceImpl(accountRepository, transactionRepository, transactionManager);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE transactions, accounts RESTART IDENTITY CASCADE");
        }
    }

    // ---------- register() ----------

    @Test
    void register_positiveCase_createsAccountWithOpeningBalance() {
        Account account = accountService.register("Aaron James", "1234", new BigDecimal("100.00"));

        assertNotNull(account.getAccountId());
        assertEquals(0, new BigDecimal("100.00").compareTo(accountService.getBalance(account.getAccountId())));
    }

    @Test
    void register_negativeCase_rejectsNonFourDigitPin() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.register("Jane Doe", "12", new BigDecimal("50.00")));
    }

    // ---------- login() ----------

    @Test
    void login_positiveCase_succeedsWithCorrectPin() {
        Account registered = accountService.register("Sam Lee", "4321", new BigDecimal("20.00"));

        Account loggedIn = accountService.login(registered.getAccountId(), "4321");

        assertEquals(registered.getAccountId(), loggedIn.getAccountId());
    }

    @Test
    void login_negativeCase_rejectsWrongPin() {
        Account registered = accountService.register("Wei Chen", "1111", new BigDecimal("20.00"));

        assertThrows(InvalidPinException.class, () -> accountService.login(registered.getAccountId(), "9999"));
    }

    // ---------- getBalance() ----------

    @Test
    void getBalance_positiveCase_returnsCurrentBalance() {
        Account registered = accountService.register("Maria Garcia", "2222", new BigDecimal("75.50"));

        assertEquals(0, new BigDecimal("75.50").compareTo(accountService.getBalance(registered.getAccountId())));
    }

    @Test
    void getBalance_negativeCase_unknownAccountThrows() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance(999_999L));
    }

    // ---------- deposit() ----------

    @Test
    void deposit_positiveCase_increasesBalance() {
        Account registered = accountService.register("Tom Baker", "3333", new BigDecimal("100.00"));

        BigDecimal newBalance = accountService.deposit(registered.getAccountId(), new BigDecimal("50.00"));

        assertEquals(0, new BigDecimal("150.00").compareTo(newBalance));
    }

    @Test
    void deposit_negativeCase_rejectsNonPositiveAmount() {
        Account registered = accountService.register("Nina Patel", "4444", new BigDecimal("100.00"));

        assertThrows(InvalidAmountException.class,
                () -> accountService.deposit(registered.getAccountId(), new BigDecimal("0")));
    }

    // ---------- withdraw() ----------

    @Test
    void withdraw_positiveCase_decreasesBalance() {
        Account registered = accountService.register("Omar Farouk", "5555", new BigDecimal("100.00"));

        BigDecimal newBalance = accountService.withdraw(registered.getAccountId(), new BigDecimal("40.00"));

        assertEquals(0, new BigDecimal("60.00").compareTo(newBalance));
    }

    @Test
    void withdraw_negativeCase_rejectsOverdraft() {
        Account registered = accountService.register("Priya Nair", "6666", new BigDecimal("30.00"));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.withdraw(registered.getAccountId(), new BigDecimal("100.00")));

        // balance must be unchanged after the rejected withdrawal
        assertEquals(0, new BigDecimal("30.00").compareTo(accountService.getBalance(registered.getAccountId())));
    }

    // ---------- transfer() ----------

    @Test
    void transfer_positiveCase_movesMoneyBetweenAccounts() {
        Account from = accountService.register("Alice", "1010", new BigDecimal("200.00"));
        Account to = accountService.register("Bob", "2020", new BigDecimal("50.00"));

        accountService.transfer(from.getAccountId(), to.getAccountId(), new BigDecimal("75.00"));

        assertEquals(0, new BigDecimal("125.00").compareTo(accountService.getBalance(from.getAccountId())));
        assertEquals(0, new BigDecimal("125.00").compareTo(accountService.getBalance(to.getAccountId())));
    }

    @Test
    void transfer_negativeCase_insufficientFundsRollsBackBothAccounts() {
        Account from = accountService.register("Carlos", "3030", new BigDecimal("10.00"));
        Account to = accountService.register("Dana", "4040", new BigDecimal("50.00"));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.transfer(from.getAccountId(), to.getAccountId(), new BigDecimal("500.00")));

        // Atomicity check: a failed transfer must leave BOTH accounts exactly as they were.
        assertEquals(0, new BigDecimal("10.00").compareTo(accountService.getBalance(from.getAccountId())));
        assertEquals(0, new BigDecimal("50.00").compareTo(accountService.getBalance(to.getAccountId())));
    }
}
