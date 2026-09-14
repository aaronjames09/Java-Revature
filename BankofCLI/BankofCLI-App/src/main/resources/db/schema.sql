-- ============================================================
-- Bank of CLI - Database Schema
-- Run this once against your target database (dev or test)
--   psql -U <user> -d <database> -f src/main/resources/db/schema.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS accounts (
    account_id      BIGSERIAL PRIMARY KEY,
    account_holder  VARCHAR(100)    NOT NULL,
    pin_hash        VARCHAR(255)    NOT NULL,
    pin_salt        VARCHAR(255)    NOT NULL,
    balance         NUMERIC(19,2)   NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id      BIGSERIAL PRIMARY KEY,
    account_id          BIGINT          NOT NULL REFERENCES accounts(account_id),
    related_account_id  BIGINT          REFERENCES accounts(account_id),
    transaction_type     VARCHAR(20)     NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER_OUT', 'TRANSFER_IN')),
    amount               NUMERIC(19,2)   NOT NULL CHECK (amount > 0),
    balance_after         NUMERIC(19,2)   NOT NULL,
    created_at            TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
