package com.aaronjames.bankofcli.service;

import com.aaronjames.bankofcli.exception.AccountNotFoundException;
import com.aaronjames.bankofcli.exception.InsufficientFundsException;
import com.aaronjames.bankofcli.exception.InvalidAmountException;
import com.aaronjames.bankofcli.exception.InvalidPinException;
import com.aaronjames.bankofcli.model.Account;
import com.aaronjames.bankofcli.model.TransactionRecord;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business operations for accounts. This is what the API/CLI layer calls -
 * it never talks to a repository or writes SQL directly.
 */
public interface AccountService {

    /**
     * Creates a new account with a hashed PIN and the given opening balance.
     * @throws IllegalArgumentException if the holder name is blank, the PIN isn't exactly 4 digits,
     *         or the initial deposit is negative
     */
    Account register(String accountHolder, String pin, BigDecimal initialDeposit);

    /**
     * @throws AccountNotFoundException if no such account exists
     * @throws InvalidPinException if the PIN doesn't match
     */
    Account login(Long accountId, String pin);

    /**
     * @throws AccountNotFoundException if no such account exists
     */
    BigDecimal getBalance(Long accountId);

    /**
     * @return the new balance after the deposit
     * @throws AccountNotFoundException if no such account exists
     * @throws InvalidAmountException if amount is zero or negative
     */
    BigDecimal deposit(Long accountId, BigDecimal amount);

    /**
     * @return the new balance after the withdrawal
     * @throws AccountNotFoundException if no such account exists
     * @throws InvalidAmountException if amount is zero or negative
     * @throws InsufficientFundsException if the account balance is less than amount
     */
    BigDecimal withdraw(Long accountId, BigDecimal amount);

    /**
     * Moves money between two accounts as a single atomic operation: either both
     * the debit and the credit succeed, or neither does.
     * @throws AccountNotFoundException if either account doesn't exist
     * @throws InvalidAmountException if amount is zero or negative
     * @throws InsufficientFundsException if the source account can't cover the transfer
     * @throws IllegalArgumentException if fromAccountId equals toAccountId
     */
    void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount);

    /**
     * @return up to {@code limit} of the account's most recent transactions, newest first
     * @throws AccountNotFoundException if no such account exists
     */
    List<TransactionRecord> getTransactionHistory(Long accountId, int limit);
}
