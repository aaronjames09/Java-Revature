package com.aaronjames.bankofcli.model;

import java.math.BigDecimal;

/**
 * Plain domain object representing a row in the "transactions" table -
 * one entry in an account's audit trail.
 */
public class TransactionRecord {

    private Long transactionId;
    private Long accountId;
    private Long relatedAccountId; // null for DEPOSIT/WITHDRAW; the other account for TRANSFER_IN/OUT
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;

    public TransactionRecord() {
    }

    public TransactionRecord(Long accountId, Long relatedAccountId, TransactionType type,
                              BigDecimal amount, BigDecimal balanceAfter) {
        this.accountId = accountId;
        this.relatedAccountId = relatedAccountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getRelatedAccountId() {
        return relatedAccountId;
    }

    public void setRelatedAccountId(Long relatedAccountId) {
        this.relatedAccountId = relatedAccountId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    @Override
    public String toString() {
        return "TransactionRecord{type=" + type + ", accountId=" + accountId
                + ", amount=" + amount + ", balanceAfter=" + balanceAfter + '}';
    }
}
