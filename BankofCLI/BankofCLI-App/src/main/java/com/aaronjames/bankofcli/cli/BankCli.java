package com.aaronjames.bankofcli.cli;

import com.aaronjames.bankofcli.exception.BankingException;
import com.aaronjames.bankofcli.exception.DataAccessException;
import com.aaronjames.bankofcli.model.Account;
import com.aaronjames.bankofcli.model.TransactionRecord;
import com.aaronjames.bankofcli.service.AccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The "API Layer" from the spec: reads terminal input, prints menus and
 * messages, and forwards every request to AccountService. It never builds a
 * repository, opens a connection, or writes SQL - if it needs the database,
 * it asks the Service layer.
 */
public class BankCli {

    private static final Logger LOGGER = Logger.getLogger(BankCli.class.getName());

    private final AccountService accountService;
    private final Scanner scanner;

    public BankCli(AccountService accountService, Scanner scanner) {
        this.accountService = accountService;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("=== Welcome to Bank of CLI ===");

        boolean running = true;
        while (running) {
            printMainMenu();
            switch (readLine()) {
                case "1" -> handleRegister();
                case "2" -> handleLogin();
                case "3" -> running = false;
                default -> System.out.println("Invalid selection. Please choose 1-3.");
            }
        }

        System.out.println("Thanks for banking with us. Goodbye!");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }

    // ---------- Registration ----------

    private void handleRegister() {
        executeSafely(() -> {
            System.out.print("Full name: ");
            String name = readLine();

            System.out.print("Choose a 4-digit PIN: ");
            String pin = readLine();

            System.out.print("Opening deposit amount: ");
            BigDecimal openingDeposit = readAmount();

            Account account = accountService.register(name, pin, openingDeposit);
            System.out.println("Account created! Your Account ID is " + account.getAccountId()
                    + " - keep this safe, you'll need it to log in.");
        });
    }

    // ---------- Login + session ----------

    private void handleLogin() {
        executeSafely(() -> {
            System.out.print("Account ID: ");
            Long accountId = readAccountId();
            if (accountId == null) {
                return;
            }

            System.out.print("PIN: ");
            String pin = readLine();

            Account account = accountService.login(accountId, pin);
            System.out.println("Welcome back, " + account.getAccountHolder() + "!");
            runAccountSession(account);
        });
    }

    private void runAccountSession(Account account) {
        boolean loggedIn = true;
        while (loggedIn) {
            printAccountMenu();
            switch (readLine()) {
                case "1" -> handleCheckBalance(account);
                case "2" -> handleDeposit(account);
                case "3" -> handleWithdraw(account);
                case "4" -> handleTransfer(account);
                case "5" -> handleHistory(account);
                case "6" -> loggedIn = false;
                default -> System.out.println("Invalid selection. Please choose 1-6.");
            }
        }
        System.out.println("Logged out.");
    }

    private void printAccountMenu() {
        System.out.println();
        System.out.println("1. Check balance");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Transaction history");
        System.out.println("6. Logout");
        System.out.print("Choose an option: ");
    }

    // ---------- Account operations ----------

    private void handleCheckBalance(Account account) {
        executeSafely(() -> {
            BigDecimal balance = accountService.getBalance(account.getAccountId());
            System.out.println("Current balance: " + balance);
        });
    }

    private void handleDeposit(Account account) {
        executeSafely(() -> {
            System.out.print("Amount to deposit: ");
            BigDecimal amount = readAmount();

            BigDecimal newBalance = accountService.deposit(account.getAccountId(), amount);
            System.out.println("Deposit successful. New balance: " + newBalance);
        });
    }

    private void handleWithdraw(Account account) {
        executeSafely(() -> {
            System.out.print("Amount to withdraw: ");
            BigDecimal amount = readAmount();

            BigDecimal newBalance = accountService.withdraw(account.getAccountId(), amount);
            System.out.println("Withdrawal successful. New balance: " + newBalance);
        });
    }

    private void handleTransfer(Account account) {
        executeSafely(() -> {
            System.out.print("Recipient Account ID: ");
            Long toAccountId = readAccountId();
            if (toAccountId == null) {
                return;
            }

            System.out.print("Amount to transfer: ");
            BigDecimal amount = readAmount();

            accountService.transfer(account.getAccountId(), toAccountId, amount);
            System.out.println("Transfer successful.");
        });
    }

    private void handleHistory(Account account) {
        executeSafely(() -> {
            List<TransactionRecord> history = accountService.getTransactionHistory(account.getAccountId(), 10);
            if (history.isEmpty()) {
                System.out.println("No transactions yet.");
                return;
            }
            System.out.println("Recent transactions:");
            for (TransactionRecord record : history) {
                System.out.println("  " + record.getType() + "  amount=" 
                + record.getAmount() + "  balance after=" + record.getBalanceAfter());
            }
        });
    }

    // ---------- Input helpers ----------

    private String readLine() {
        return scanner.nextLine().trim();
    }

    /** @return the parsed amount, or null if input was invalid (caller already saw a friendly message) */
    private BigDecimal readAmount() {
        String input = readLine();
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + input + "' is not a valid amount");
        }
    }

    private Long readAccountId() {
        String input = readLine();
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            System.out.println("'" + input + "' is not a valid Account ID.");
            return null;
        }
    }

    /**
     * Every menu action goes through here. This is where the spec's "no stack traces to the
     * user" rule is actually enforced: a BankingException means the operation was understood
     * and rejected for a business reason, so its message goes straight to the user. Anything
     * else (a DataAccessException, or a genuinely unexpected bug) gets logged in full and the
     * user sees a generic, non-technical message instead.
     */
    private void executeSafely(Runnable action) {
        try {
            action.run();
        } catch (BankingException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        } catch (DataAccessException e) {
            LOGGER.log(Level.SEVERE, "Database operation failed", e);
            System.out.println("Service currently unavailable. Please try again later.");
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            System.out.println("Something went wrong. Please try again later.");
        }
    }
}
