package com.aaronjames.bankofcli.service;

import com.aaronjames.bankofcli.db.TransactionManager;
import com.aaronjames.bankofcli.exception.AccountNotFoundException;
import com.aaronjames.bankofcli.exception.InsufficientFundsException;
import com.aaronjames.bankofcli.exception.InvalidAmountException;
import com.aaronjames.bankofcli.exception.InvalidPinException;
import com.aaronjames.bankofcli.model.Account;
import com.aaronjames.bankofcli.model.TransactionRecord;
import com.aaronjames.bankofcli.model.TransactionType;
import com.aaronjames.bankofcli.repository.AccountRepository;
import com.aaronjames.bankofcli.repository.TransactionRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Implements every banking rule from the spec:
 *   - PIN-based auth (register/login)
 *   - overdraft prevention on withdraw
 *   - atomic, all-or-nothing transfers
 *   - an audit trail entry for every money movement
 *
 * Depends only on the AccountRepository / TransactionRepository /
 * TransactionManager INTERFACES (Dependency Inversion) - none of this logic
 * knows or cares that the data lives in PostgreSQL specifically.
 */
public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER = Logger.getLogger(AccountServiceImpl.class.getName());
    private static final Pattern PIN_PATTERN = Pattern.compile("\\d{4}");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionManager transactionManager;

    public AccountServiceImpl(AccountRepository accountRepository,
                               TransactionRepository transactionRepository,
                               TransactionManager transactionManager) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionManager = transactionManager;
    }

    @Override
    public Account register(String accountHolder, String pin, BigDecimal initialDeposit) {
        if (accountHolder == null || accountHolder.isBlank()) {
            throw new IllegalArgumentException("Account holder name must not be blank");
        }
        if (pin == null || !PIN_PATTERN.matcher(pin).matches()) {
            throw new IllegalArgumentException("PIN must be exactly 4 digits");
        }
        if (initialDeposit == null || initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial deposit must not be negative");
        }

        Account account = new Account(accountHolder, pin, initialDeposit);

        Account saved = transactionManager.executeInTransaction(connection ->
                accountRepository.save(account, connection));

        LOGGER.info("Registered new account id=" + saved.getAccountId() + " for '" + accountHolder + "'");
        return saved;
    }

    @Override
    public Account login(Long accountId, String pin) {
        Account account = transactionManager.executeInTransaction(connection ->
                requireAccount(accountId, connection));

        if (!pin.equals(account.getPin())) {
            LOGGER.warning("Incorrect PIN attempt for account id=" + accountId);
            throw new InvalidPinException();
        }

        LOGGER.info("Successful login for account id=" + accountId);
        return account;
    }

    @Override
    public BigDecimal getBalance(Long accountId) {
        Account account = transactionManager.executeInTransaction(connection ->
                requireAccount(accountId, connection));
        return account.getBalance();
    }

    @Override
    public BigDecimal deposit(Long accountId, BigDecimal amount) {
        requirePositiveAmount(amount);

        BigDecimal newBalance = transactionManager.executeInTransaction(connection -> {
            Account account = requireAccount(accountId, connection);
            BigDecimal updatedBalance = account.getBalance().add(amount);

            accountRepository.updateBalance(accountId, updatedBalance, connection);
            transactionRepository.record(
                    new TransactionRecord(accountId, null, TransactionType.DEPOSIT, amount, updatedBalance),
                    connection);

            return updatedBalance;
        });

        LOGGER.info("Deposited " + amount + " into account id=" + accountId + ", new balance=" + newBalance);
        return newBalance;
    }

    @Override
    public BigDecimal withdraw(Long accountId, BigDecimal amount) {
        requirePositiveAmount(amount);

        BigDecimal newBalance = transactionManager.executeInTransaction(connection -> {
            Account account = requireAccount(accountId, connection);

            if (account.getBalance().compareTo(amount) < 0) {
                LOGGER.warning("Rejected overdraft attempt on account id=" + accountId
                        + ": balance=" + account.getBalance() + ", requested=" + amount);
                throw new InsufficientFundsException(accountId, amount, account.getBalance());
            }

            BigDecimal updatedBalance = account.getBalance().subtract(amount);
            accountRepository.updateBalance(accountId, updatedBalance, connection);
            transactionRepository.record(
                    new TransactionRecord(accountId, null, TransactionType.WITHDRAW, amount, updatedBalance),
                    connection);

            return updatedBalance;
        });

        LOGGER.info("Withdrew " + amount + " from account id=" + accountId + ", new balance=" + newBalance);
        return newBalance;
    }

    @Override
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        requirePositiveAmount(amount);
        if (fromAccountId != null && fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Everything below runs against ONE connection/transaction: if any line throws,
        // JdbcTransactionManager rolls back both balance updates and both audit rows,
        // so a failed transfer never leaves money deducted from only one side.
        transactionManager.executeInTransaction(connection -> {
            Account fromAccount = requireAccount(fromAccountId, connection);
            Account toAccount = requireAccount(toAccountId, connection);

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                LOGGER.warning("Rejected transfer from account id=" + fromAccountId
                        + ": balance=" + fromAccount.getBalance() + ", requested=" + amount);
                throw new InsufficientFundsException(fromAccountId, amount, fromAccount.getBalance());
            }

            BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
            BigDecimal newToBalance = toAccount.getBalance().add(amount);

            accountRepository.updateBalance(fromAccountId, newFromBalance, connection);
            accountRepository.updateBalance(toAccountId, newToBalance, connection);

            transactionRepository.record(new TransactionRecord(
                    fromAccountId, toAccountId, TransactionType.TRANSFER_OUT, amount, newFromBalance), connection);
            transactionRepository.record(new TransactionRecord(
                    toAccountId, fromAccountId, TransactionType.TRANSFER_IN, amount, newToBalance), connection);

            return null;
        });

        LOGGER.info("Transferred " + amount + " from account id=" + fromAccountId
                + " to account id=" + toAccountId);
    }

    @Override
    public List<TransactionRecord> getTransactionHistory(Long accountId, int limit) {
        return transactionManager.executeInTransaction(connection -> {
            requireAccount(accountId, connection); // fail fast if the account doesn't exist
            return transactionRepository.findRecentByAccountId(accountId, limit, connection);
        });
    }

    private Account requireAccount(Long accountId, Connection connection) {
        Optional<Account> account = accountRepository.findById(accountId, connection);
        return account.orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(amount);
        }
    }
}
