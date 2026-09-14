package com.aaronjames.bankofcli.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends BankingException {
    public InsufficientFundsException(Long accountId, BigDecimal requested, BigDecimal available) {
        super("Account " + accountId + " has insufficient funds: requested " + requested
                + " but only " + available + " is available");
    }
}
