package com.aaronjames.bankofcli.exception;

/**
 * Base type for business-rule violations raised by the Service layer
 * (wrong PIN, insufficient funds, account not found, invalid amount, etc).
 *
 * These are deliberately a DIFFERENT type from DataAccessException:
 * a BankingException means "the operation was correctly understood and
 * rejected for a business reason" (show the user a specific, helpful
 * message), while a DataAccessException means "something technical broke"
 * (show the user a generic "service unavailable" message and log the
 * detail). The CLI/API layer can tell them apart and react accordingly.
 */
public class BankingException extends RuntimeException {
    public BankingException(String message) {
        super(message);
    }
}
