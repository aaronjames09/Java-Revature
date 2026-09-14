package com.aaronjames.bankofcli;

import com.aaronjames.bankofcli.cli.BankCli;
import com.aaronjames.bankofcli.config.ConfigLoader;
import com.aaronjames.bankofcli.config.DatabaseConfig;
import com.aaronjames.bankofcli.db.ConnectionProvider;
import com.aaronjames.bankofcli.db.JdbcTransactionManager;
import com.aaronjames.bankofcli.db.PostgresConnectionProvider;
import com.aaronjames.bankofcli.db.TransactionManager;
import com.aaronjames.bankofcli.repository.AccountRepository;
import com.aaronjames.bankofcli.repository.PostgresAccountRepository;
import com.aaronjames.bankofcli.repository.PostgresTransactionRepository;
import com.aaronjames.bankofcli.repository.TransactionRepository;
import com.aaronjames.bankofcli.service.AccountService;
import com.aaronjames.bankofcli.service.AccountServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Application entry point and composition root.
 *
 * This is the ONLY class that knows about every layer at once and wires
 * them together. Every other class depends on interfaces (AccountRepository,
 * TransactionManager, AccountService, ...) and never instantiates its own
 * dependencies - that happens here, once, at startup.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        configureLogging();
        Logger logger = Logger.getLogger(Main.class.getName());

        DatabaseConfig databaseConfig;
        try {
            databaseConfig = ConfigLoader.loadDatabaseConfig("application.properties");
        } catch (ConfigLoader.ConfigurationException e) {
            // Config problems happen before we're "in" the app yet - fine to show the
            // technical detail directly here, there's no end user session to protect.
            System.out.println("Startup failed: " + e.getMessage());
            return;
        }

        ConnectionProvider connectionProvider = new PostgresConnectionProvider(databaseConfig);
        TransactionManager transactionManager = new JdbcTransactionManager(connectionProvider);
        AccountRepository accountRepository = new PostgresAccountRepository();
        TransactionRepository transactionRepository = new PostgresTransactionRepository();
        AccountService accountService =
                new AccountServiceImpl(accountRepository, transactionRepository, transactionManager);

        logger.info("Bank of CLI starting up");

        try (Scanner scanner = new Scanner(System.in)) {
            new BankCli(accountService, scanner).run();
        }

        logger.info("Bank of CLI shut down");
    }

    private static void configureLogging() {
        try {
            Files.createDirectories(Path.of("logs"));

            try (InputStream configStream = Main.class.getClassLoader().getResourceAsStream("logging.properties")) {
                if (configStream != null) {
                    LogManager.getLogManager().readConfiguration(configStream);
                }
            }
        } catch (IOException e) {
            // Logging config failing shouldn't crash the whole app - it'll just fall back
            // to the JVM's default console logging instead of writing to logs/bank-of-cli.log.
            System.out.println("Warning: could not load logging configuration (" + e.getMessage() + ")");
        }
    }
}
