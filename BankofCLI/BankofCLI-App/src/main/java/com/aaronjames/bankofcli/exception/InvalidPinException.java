package com.aaronjames.bankofcli.exception;

public class InvalidPinException extends BankingException {
    public InvalidPinException() {
        super("Incorrect PIN");
    }
}
