package com.aaronjames.bankofcli.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Plain domain object representing a row in the "accounts" table.
 * No SQL or business logic lives here on purpose - this is just data.
 */
public class Account {

    private Long accountId;
    private String accountHolder;
    private String pinHash;
    private String pinSalt;
    private BigDecimal balance;
    private Timestamp createdAt;

    public Account() {
    }

    public Account(String accountHolder, String pinHash, String pinSalt, BigDecimal balance) {
        this.accountHolder = accountHolder;
        this.pinHash = pinHash;
        this.pinSalt = pinSalt;
        this.balance = balance;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public String getPinSalt() {
        return pinSalt;
    }

    public void setPinSalt(String pinSalt) {
        this.pinSalt = pinSalt;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return Objects.equals(accountId, account.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        return "Account{accountId=" + accountId + ", accountHolder='" + accountHolder
                + "', balance=" + balance + '}';
    }
}
