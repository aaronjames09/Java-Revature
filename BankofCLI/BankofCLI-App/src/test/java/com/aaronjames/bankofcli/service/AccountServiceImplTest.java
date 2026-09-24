package com.aaronjames.bankofcli.service;

import com.aaronjames.bankofcli.db.TransactionManager;
import com.aaronjames.bankofcli.db.UnitOfWork;
import com.aaronjames.bankofcli.exception.DataAccessException;
import com.aaronjames.bankofcli.exception.InsufficientFundsException;
import com.aaronjames.bankofcli.model.Account;
import com.aaronjames.bankofcli.model.TransactionRecord;
import com.aaronjames.bankofcli.repository.AccountRepository;
import com.aaronjames.bankofcli.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountServiceImpl's withdraw() logic, using fake
 * AccountRepository/TransactionRepository/TransactionManager implementations
 * instead of a real database - exactly the substitution these interfaces
 * were built to support (Dependency Inversion).
 */
class AccountServiceImplTest {

    private FakeAccountRepository accountRepository;
    private FakeTransactionRepository transactionRepository;
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountRepository = new FakeAccountRepository();
        transactionRepository = new FakeTransactionRepository();
        accountService = new AccountServiceImpl(
                accountRepository, transactionRepository, new FakeTransactionManager());
    }

    @Test
    void withdraw_withSufficientBalance_succeedsAndDecreasesBalance() {
        Account account = new Account("Carolin", "1234", new BigDecimal("100.00"));
        account.setAccountId(1L);
        accountRepository.seed(account);

        BigDecimal newBalance = accountService.withdraw(1L, new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), newBalance);
        assertEquals(new BigDecimal("60.00"), accountRepository.findById(1L, null).orElseThrow().getBalance());
        assertEquals(1, transactionRepository.getAll().size());
    }

    @Test
    void withdraw_withInsufficientBalance_throwsAndLeavesBalanceUnchanged() {
        Account account = new Account("Carolin", "1234", new BigDecimal("50.00"));
        account.setAccountId(1L);
        accountRepository.seed(account);

        assertThrows(InsufficientFundsException.class,
                () -> accountService.withdraw(1L, new BigDecimal("100.00")));

        assertEquals(new BigDecimal("50.00"), accountRepository.findById(1L, null).orElseThrow().getBalance());
        assertTrue(transactionRepository.getAll().isEmpty());
    }

    // ---- Fakes: in-memory stand-ins for the real repositories/manager ----

    private static class FakeAccountRepository implements AccountRepository {
        private final Map<Long, Account> accounts = new HashMap<>();
        private long nextId = 1;

        void seed(Account account) {
            accounts.put(account.getAccountId(), account);
        }

        @Override
        public Account save(Account account, Connection connection) {
            account.setAccountId(nextId++);
            accounts.put(account.getAccountId(), account);
            return account;
        }

        @Override
        public Optional<Account> findById(Long accountId, Connection connection) {
            return Optional.ofNullable(accounts.get(accountId));
        }

        @Override
        public void updateBalance(Long accountId, BigDecimal newBalance, Connection connection) {
            Account account = accounts.get(accountId);
            if (account == null) {
                throw new DataAccessException("No account found with id " + accountId);
            }
            account.setBalance(newBalance);
        }
    }

    private static class FakeTransactionRepository implements TransactionRepository {
        private final List<TransactionRecord> records = new ArrayList<>();
        private long nextId = 1;

        List<TransactionRecord> getAll() {
            return records;
        }

        @Override
        public TransactionRecord record(TransactionRecord transactionRecord, Connection connection) {
            transactionRecord.setTransactionId(nextId++);
            records.add(transactionRecord);
            return transactionRecord;
        }

        @Override
        public List<TransactionRecord> findRecentByAccountId(Long accountId, int limit, Connection connection) {
            return records.stream()
                    .filter(r -> r.getAccountId().equals(accountId))
                    .toList();
        }
    }

    private static class FakeTransactionManager implements TransactionManager {
        @Override
        public <T> T executeInTransaction(UnitOfWork<T> unitOfWork) {
            return unitOfWork.execute(null);
        }
    }
}