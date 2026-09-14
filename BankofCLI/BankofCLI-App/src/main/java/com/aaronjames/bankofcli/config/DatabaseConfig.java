package com.aaronjames.bankofcli.config;

/**
 * Immutable holder for the settings needed to open a database connection.
 * Kept as a plain data object so every other class depends on this simple
 * type rather than on how the values were loaded (file, env vars, etc).
 */
public final class DatabaseConfig {

    private final String url;
    private final String username;
    private final String password;

    public DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
