package com.aaronjames.bankofcli.exception;

public class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(Long accountId) {
        super("No account found with ID " + accountId);
    }
}
