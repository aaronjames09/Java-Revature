package com.aaronjames.bankofcli.repository;

import com.aaronjames.bankofcli.exception.DataAccessException;
import com.aaronjames.bankofcli.model.TransactionRecord;
import com.aaronjames.bankofcli.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgresTransactionRepository implements TransactionRepository {

    private static final Logger LOGGER = Logger.getLogger(PostgresTransactionRepository.class.getName());

    @Override
    public TransactionRecord record(TransactionRecord transactionRecord, Connection connection) {
        if (transactionRecord == null) {
            throw new IllegalArgumentException("transactionRecord must not be null");
        }

        String sql = "INSERT INTO transactions "
                + "(account_id, related_account_id, transaction_type, amount, balance_after) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, transactionRecord.getAccountId());
            if (transactionRecord.getRelatedAccountId() != null) {
                statement.setLong(2, transactionRecord.getRelatedAccountId());
            } else {
                statement.setNull(2, Types.BIGINT);
            }
            statement.setString(3, transactionRecord.getType().name());
            statement.setBigDecimal(4, transactionRecord.getAmount());
            statement.setBigDecimal(5, transactionRecord.getBalanceAfter());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transactionRecord.setTransactionId(generatedKeys.getLong(1));
                }
            }

            LOGGER.info("Recorded " + transactionRecord.getType() + " for account id="
                    + transactionRecord.getAccountId() + " amount=" + transactionRecord.getAmount());
            return transactionRecord;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to record transaction for account id="
                    + transactionRecord.getAccountId(), e);
            throw new DataAccessException("Could not record transaction", e);
        }
    }

    @Override
    public List<TransactionRecord> findRecentByAccountId(Long accountId, int limit, Connection connection) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId must not be null");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }

        String sql = "SELECT transaction_id, account_id, related_account_id, transaction_type, "
                + "amount, balance_after, created_at "
                + "FROM transactions WHERE account_id = ? ORDER BY created_at DESC, transaction_id DESC LIMIT ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.setInt(2, limit);

            List<TransactionRecord> results = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }
            }
            return results;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to look up transaction history for account id=" + accountId, e);
            throw new DataAccessException("Could not retrieve transaction history", e);
        }
    }

    private TransactionRecord mapRow(ResultSet resultSet) throws SQLException {
        TransactionRecord record = new TransactionRecord();
        record.setTransactionId(resultSet.getLong("transaction_id"));
        record.setAccountId(resultSet.getLong("account_id"));

        long relatedAccountId = resultSet.getLong("related_account_id");
        record.setRelatedAccountId(resultSet.wasNull() ? null : relatedAccountId);

        record.setType(TransactionType.valueOf(resultSet.getString("transaction_type")));
        record.setAmount(resultSet.getBigDecimal("amount"));
        record.setBalanceAfter(resultSet.getBigDecimal("balance_after"));
        return record;
    }
}
