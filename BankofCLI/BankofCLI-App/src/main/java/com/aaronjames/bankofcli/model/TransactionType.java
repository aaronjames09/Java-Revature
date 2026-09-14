package com.aaronjames.bankofcli.model;

/**
 * Matches the transaction_type CHECK constraint in the transactions table.
 * TRANSFER_OUT/TRANSFER_IN are two separate audit rows (one per account)
 * for a single transfer, linked via related_account_id.
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER_OUT,
    TRANSFER_IN
}
