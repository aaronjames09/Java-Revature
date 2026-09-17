# Bank of CLI

A terminal-based Java banking application with a layered architecture, atomic money transfers, an audit trail, and system logging — backed by PostgreSQL.

## Overview

**Bank of CLI** simulates the core functionality of a banking system: registering accounts, authenticating with a PIN, and performing deposits, withdrawals, and transfers, all recorded in a transaction history.

The project demonstrates **Java, JDBC, PostgreSQL, Maven, Git, and GitHub**, built around a strict layered architecture that keeps the terminal UI, business rules, and database access fully separate from each other.

## Status

**MVP complete.** Register, login, balance check, deposit, withdraw, transfer, and transaction history all work end-to-end against a running PostgreSQL database.

## Features

- **Register** a new account with a full name, a 4-digit PIN, and an opening deposit
- **Log in** using an Account ID and PIN
- **Check balance** at any time while logged in
- **Deposit** funds
- **Withdraw** funds, with overdrafts rejected
- **Transfer** funds between two accounts — atomic, so a transfer either fully succeeds or leaves both accounts untouched
- **Transaction history** — the 10 most recent deposits, withdrawals, and transfers for the logged-in account
- **System logging** — INFO for successful operations, SEVERE for failures, written to `logs/bank-of-cli.log.0`

## Architecture

```text
┌──────────────────────────────┐
│           CLI Layer          │
│   Terminal menus / I/O       │
└──────────────┬───────────────┘
               │  calls only
               ▼
┌──────────────────────────────┐
│        Service Layer         │
│   Business rules, validation │
└──────────────┬───────────────┘
               │  calls only
               ▼
┌──────────────────────────────┐
│      Repository Layer        │
│   SQL via a handed-in         │
│   Connection                 │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│          PostgreSQL           │
└──────────────────────────────┘
```

Each layer only calls the one directly below it. `Main.java` is the **composition root** — the only class that knows about every layer at once and wires concrete implementations (`PostgresAccountRepository`, `JdbcTransactionManager`, etc.) into the interfaces everything else depends on. This is Dependency Inversion applied throughout: the Service layer depends on `AccountRepository` and `TransactionManager` interfaces, never on JDBC or PostgreSQL directly.

### Transaction atomicity

A transfer between two accounts — debit one, credit the other, write two audit rows — runs as a single database transaction via `JdbcTransactionManager`. If any step fails, everything rolls back and both accounts are left exactly as they were beforehand. `JdbcTransactionManager` is the only class in the application that calls `commit()` or `rollback()`; every repository method just executes SQL against whatever `Connection` it's handed, which is what makes multi-step operations safe to compose without duplicating transaction logic.

### Error handling

- **Business errors** (`AccountNotFoundException`, `InvalidPinException`, `InsufficientFundsException`, `InvalidAmountException`) are understood, expected failures — the user sees a specific, helpful message.
- **Technical errors** (`DataAccessException` — a lost database connection, a failed query) never leak SQL or stack traces to the user. They see a generic `Service currently unavailable. Please try again later.` while the real detail is logged at SEVERE.

## Tech Stack

| Technology     | Purpose                          |
| -------------- | --------------------------------- |
| **Java 21**    | Application development (targets Java 17 bytecode) |
| **Maven**      | Build and dependency management   |
| **PostgreSQL 18** | Persistent database (Dockerized) |
| **Docker Compose** | Local database environment   |
| **Git / GitHub** | Version control                 |

## Project Structure

```text
BankofCLI-App/
├── docker/
│   └── docker-compose.yml           — Postgres 18 dev database
├── src/main/java/com/aaronjames/bankofcli/
│   ├── Main.java                            — composition root / entry point
│   ├── cli/BankCli.java                     — terminal menus
│   ├── config/DatabaseConfig.java
│   ├── config/ConfigLoader.java
│   ├── db/ConnectionProvider.java (interface)
│   ├── db/PostgresConnectionProvider.java
│   ├── db/TransactionManager.java (interface)
│   ├── db/JdbcTransactionManager.java
│   ├── db/UnitOfWork.java (functional interface)
│   ├── model/Account.java
│   ├── model/TransactionRecord.java
│   ├── model/TransactionType.java (enum: DEPOSIT, WITHDRAW, TRANSFER_OUT, TRANSFER_IN)
│   ├── exception/DataAccessException.java
│   ├── exception/BankingException.java
│   ├── exception/AccountNotFoundException.java
│   ├── exception/InvalidPinException.java
│   ├── exception/InsufficientFundsException.java
│   ├── exception/InvalidAmountException.java
│   ├── repository/AccountRepository.java (interface)
│   ├── repository/PostgresAccountRepository.java
│   ├── repository/TransactionRepository.java (interface)
│   ├── repository/PostgresTransactionRepository.java
│   └── service/AccountService.java (interface)
│       service/AccountServiceImpl.java
├── src/main/resources/
│   ├── application.properties.example    — copy to application.properties (gitignored)
│   ├── logging.properties
│   └── db/schema.sql
├── pom.xml
└── .gitignore
```

## Database Schema

**accounts**
| Column | Type | Notes |
|---|---|---|
| account_id | BIGSERIAL | PK |
| account_holder | VARCHAR(100) | |
| pin | VARCHAR(4) | |
| balance | NUMERIC(19,2) | ≥ 0, enforced by a CHECK constraint |

**transactions**
| Column | Type | Notes |
|---|---|---|
| transaction_id | BIGSERIAL | PK |
| account_id | BIGINT | FK → accounts, the owning account |
| related_account_id | BIGINT | FK → accounts, nullable — the counterparty on a transfer |
| transaction_type | VARCHAR(20) | DEPOSIT, WITHDRAW, TRANSFER_OUT, TRANSFER_IN |
| amount | NUMERIC(19,2) | > 0, enforced by a CHECK constraint |
| balance_after | NUMERIC(19,2) | the account's balance immediately after this transaction |

A transfer writes two rows — a `TRANSFER_OUT` on the source account and a `TRANSFER_IN` on the destination account — each pointing at the other account via `related_account_id`. Transaction history is ordered by `transaction_id DESC`, which reflects insertion order since it's a `BIGSERIAL`.

## Getting Started

### Prerequisites

- Java JDK 17+
- Maven
- Docker Desktop
- Git

### Clone the repository

```bash
git clone https://github.com/aaronjames09/Java-Revature.git
cd Java-Revature/BankofCLI/BankofCLI-App
```

### Start the database

```bash
cd docker
docker compose up -d
cd ..
```

This starts a PostgreSQL 18 container and runs `schema.sql` automatically on first startup.

### Configure your credentials

Copy `src/main/resources/application.properties.example` to `application.properties` in the same folder, and fill in the values matching `docker-compose.yml`:

```properties
db.url=jdbc:postgresql://localhost:5432/bankofcli
db.username=bankofcli_user
db.password=<your POSTGRES_PASSWORD from docker-compose.yml>
```

`application.properties` is gitignored — your credentials never get committed.

### Build

```bash
mvn clean package
```

This produces a runnable fat jar (PostgreSQL driver included) at `target/bank-of-cli.jar`.

### Run

```bash
java -jar target/bank-of-cli.jar
```

## Project Goals

This project demonstrates practical experience with:

- Object-oriented Java and layered application design
- Dependency Inversion via interfaces at every layer boundary
- JDBC and atomic multi-statement database transactions
- PostgreSQL schema design and Docker-based local development
- Maven project and dependency management
- Structured exception handling (business vs. technical failures)
- Application logging
- Git version control