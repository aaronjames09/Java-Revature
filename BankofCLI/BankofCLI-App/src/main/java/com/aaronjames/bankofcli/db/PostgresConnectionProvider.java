package com.aaronjames.bankofcli.db;

import com.aaronjames.bankofcli.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens a plain JDBC connection to PostgreSQL using DriverManager.
 *
 * Deliberately simple for now (no pooling) - a connection pool like HikariCP
 * can be dropped in later as a second implementation of ConnectionProvider
 * without changing a single repository class.
 */
public class PostgresConnectionProvider implements ConnectionProvider {

    private final DatabaseConfig config;

    public PostgresConnectionProvider(DatabaseConfig config) {
        this.config = config;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
    }
}
