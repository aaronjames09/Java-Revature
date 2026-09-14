package com.aaronjames.bankofcli.exception;

/**
 * Thrown by the Repository layer whenever a database operation fails.
 *
 * The Service layer catches this, logs the technical detail (message/cause)
 * at ERROR level, and shows the end user a friendly "Service currently
 * unavailable" message instead of a raw SQLException / stack trace.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
