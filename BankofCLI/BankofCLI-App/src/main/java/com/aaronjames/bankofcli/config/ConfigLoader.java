package com.aaronjames.bankofcli.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigLoader {

    private ConfigLoader() {
    }

    public static DatabaseConfig loadDatabaseConfig(String resourceName) {
        Properties properties = new Properties();

        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new ConfigurationException(
                        "Could not find '" + resourceName + "' on the classpath. "
                        + "Copy '" + resourceName + ".example' to '" + resourceName
                        + "' in the same folder and fill in your real database details.");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read '" + resourceName + "'", e);
        }

        String url = requireProperty(properties, "db.url", resourceName);
        String username = requireProperty(properties, "db.username", resourceName);
        String password = requireProperty(properties, "db.password", resourceName);
        return new DatabaseConfig(url, username, password);
    }

    private static String requireProperty(Properties properties, String key, String resourceName) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Missing required property '" + key + "' in " + resourceName);
        }
        return value;
    }

    public static class ConfigurationException extends RuntimeException {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
