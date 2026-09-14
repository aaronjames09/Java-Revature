package com.aaronjames.bankofcli.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Anything that can hand out a database Connection.
 *
 * Repository classes depend on THIS interface, never on DriverManager or a
 * connection pool directly (Dependency Inversion). That means:
 *   - we can swap DriverManager for a connection pool later without touching
 *     any repository code
 *   - repository unit tests can supply a fake/mock provider if needed
 */
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
