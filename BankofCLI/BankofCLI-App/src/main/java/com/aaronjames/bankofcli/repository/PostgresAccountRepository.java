package com.aaronjames.bankofcli.repository;

import com.aaronjames.bankofcli.exception.DataAccessException;
import com.aaronjames.bankofcli.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC-based implementation of AccountRepository, talking to PostgreSQL.
 *
 * Stateless on purpose - it holds no Connection field. Every method receives
 * the Connection to use as a parameter and never commits, rolls back, or
 * closes it. That's the caller's responsibility (typically a
 * TransactionManager), which is what makes multi-step, multi-account
 * operations like transfers atomic.
 */
public class PostgresAccountRepository implements AccountRepository {

    private static final Logger LOGGER = Logger.getLogger(PostgresAccountRepository.class.getName());

    @Override
    public Account save(Account account, Connection connection) {
        if (account == null) {
            throw new IllegalArgumentException("account must not be null");
        }

        String sql = "INSERT INTO accounts (account_holder, pin, balance) "
                + "VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, account.getAccountHolder());
            statement.setString(2, account.getPin());
            statement.setBigDecimal(3, account.getBalance());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setAccountId(generatedKeys.getLong(1));
                }
            }

            LOGGER.info("Created account id=" + account.getAccountId());
            return account;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save account for holder '" + account.getAccountHolder() + "'", e);
            throw new DataAccessException("Could not create account", e);
        }
    }

    @Override
    public Optional<Account> findById(Long accountId, Connection connection) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId must not be null");
        }

        String sql = "SELECT account_id, account_holder, pin, balance "
                + "FROM accounts WHERE account_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to look up account id=" + accountId, e);
            throw new DataAccessException("Could not retrieve account", e);
        }
    }

    @Override
    public void updateBalance(Long accountId, BigDecimal newBalance, Connection connection) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId must not be null");
        }
        if (newBalance == null) {
            throw new IllegalArgumentException("newBalance must not be null");
        }

        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, newBalance);
            statement.setLong(2, accountId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DataAccessException("No account found with id " + accountId);
            }

            LOGGER.info("Updated balance for account id=" + accountId + " to " + newBalance);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update balance for account id=" + accountId, e);
            throw new DataAccessException("Could not update account balance", e);
        }
    }

    private Account mapRow(ResultSet resultSet) throws SQLException {
        Account account = new Account();
        account.setAccountId(resultSet.getLong("account_id"));
        account.setAccountHolder(resultSet.getString("account_holder"));
        account.setPin(resultSet.getString("pin"));
        account.setBalance(resultSet.getBigDecimal("balance"));
        return account;
    }
}
