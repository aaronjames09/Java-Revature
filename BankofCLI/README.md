# Project : Bank of CLI

A Java-based banking application built with a focus on secure account management, transactional integrity, database persistence and layered architecture.

## 📋 Overview

**Bank of CLI** is a terminal-based banking application designed to simulate the core functionality of a banking system.

The project demonstrates the use of **Java, SQL, Maven, PostgreSQL, JUnit 5, Git and GitHub** while following a layered architecture that separates the user interface, business logic and database operations.

The goal is to build a reliable **Core Ledger** capable of managing accounts and financial transactions while maintaining an audit trail and system logs.

---

## 🎯 MVP Features

### 🔐 Secure Access

Users will be able to:

* Register a new account
* Log in using a unique **Account ID**
* Authenticate using a **PIN**
* Receive user-friendly messages when authentication fails

### 💰 Balance Management

Authenticated users will be able to:

* View their current account balance
* Access their balance at any time through the CLI

### 💳 Transaction Engine

The application will support:

#### Deposit

Add funds to an account.

#### Withdraw

Remove funds from an account while preventing overdrafts.

#### Transfer

Securely transfer funds between two different accounts.

Transfers are **atomic**, meaning the entire transaction succeeds or fails as one unit. Money must never be deducted from one account without being successfully added to the destination account.

### 📜 Audit Trail

Users will be able to view their recent transaction history, including account activity such as:

* Deposits
* Withdrawals
* Transfers

### 📝 System Logging

The application will maintain a log file to track system activity.

Two logging levels are required:

* `INFO` — Records successful operations such as successful logins and completed transactions.
* `ERROR` — Records failures, invalid operations, security risks or system problems such as incorrect PIN attempts or database connection failures.

---

## 🏗️ Architecture

Bank of CLI will follow a **Layered Architecture**.

```text
┌──────────────────────────────┐
│          API Layer           │
│                              │
│  CLI Input / Menus / Output  │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│        Service Layer         │
│                              │
│     Business Logic / Rules   │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│       Repository Layer       │
│                              │
│     SQL / Database Access    │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│          PostgreSQL          │
│                              │
│       Persistent Data        │
└──────────────────────────────┘
```

### 1. API Layer

The API layer is responsible for everything the user interacts with through the terminal.

Responsibilities include:

* Reading terminal input
* Displaying menus
* Navigating the application
* Displaying user-friendly messages
* Sending requests to the Service Layer

The API Layer **only communicates with the Service Layer**.

### 2. Service Layer

The Service Layer contains the application's business rules and logic.

Responsibilities include:

* Validating banking operations
* Enforcing account rules
* Preventing overdrafts
* Processing deposits and withdrawals
* Managing transfers
* Handling authentication logic
* Calling the Repository Layer

The Service Layer **does not directly interact with the user interface or database**.

### 3. Repository Layer

The Repository Layer is responsible for communication with PostgreSQL.

Responsibilities include:

* Executing SQL queries
* Reading data from the database
* Saving data to the database
* Converting database rows into Java objects
* Converting Java objects into database records

The Repository Layer **only receives requests from the Service Layer**.

---

## 🛠️ Tech Stack

| Technology     | Purpose                         |
| -------------- | ------------------------------- |
| **Java**       | Application development         |
| **Maven**      | Build and dependency management |
| **PostgreSQL** | Persistent database             |
| **JUnit 5**    | Automated testing               |
| **Git**        | Version control                 |
| **GitHub**     | Remote repository               |

---

## 🧪 Testing Requirements

Bank of CLI follows a **"2-Test Rule"** for methods in the Service and Repository layers.

Every method must have at least two JUnit 5 tests:

### ✅ Positive Test

Verifies that the method behaves correctly when the operation succeeds.

Example:

```text
Deposit $100 into an account
Expected result: Account balance increases by $100
```

### ❌ Negative Test

Verifies that the method handles invalid or unsuccessful operations correctly.

Example:

```text
Withdraw more money than the account contains
Expected result: Withdrawal is rejected and the balance remains unchanged
```

This testing approach helps ensure that both expected behavior and error handling are verified.

---

## 🔒 Transaction Atomicity

Transfers must follow the **All-or-Nothing** principle.

For example:

```text
Account A: -$100
       +
Account B: +$100
       =
Successful Transfer
```

If the deposit into Account B fails, the withdrawal from Account A must also be rolled back.

```text
Transfer Fails
      ↓
Rollback
      ↓
Account A: unchanged
Account B: unchanged
```

This prevents money from being lost or created during a failed transfer.

---

## ⚠️ Error Handling

The application should provide clear and user-friendly error handling.

### User Errors

When users make mistakes, the application should provide helpful messages.

Examples:

```text
Incorrect PIN
Insufficient funds
Account not found
Invalid menu selection
Invalid transfer amount
```

### System Errors

Technical problems should not expose database errors or Java stack traces directly to users.

For example, if PostgreSQL becomes unavailable:

```text
User sees:

Service currently unavailable. Please try again later.
```

While the system log records the technical problem:

```text
ERROR: Database connection lost
```

This keeps the application both **user-friendly and secure**.

---

## 📁 Planned Project Structure

```text
BankofCLI/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── ...
│   │
│   └── test/
│       └── java/
│           └── ...
│
├── logs/
│   └── ...
│
├── pom.xml
├── README.md
└── ...
```

The exact package and class structure will be developed as the project progresses.

---

## 🚀 Getting Started

### Prerequisites

Before running the application, make sure you have:

* Java JDK installed
* Maven installed
* PostgreSQL installed and running
* Git installed

### Clone the Repository

```bash
git clone https://github.com/aaronjames09/Java-Revature.git
```

### Navigate to the Project

```bash
cd Java-Revature/BankofCLI
```

### Build the Project

```bash
mvn clean install
```

### Run Tests

```bash
mvn test
```

### Run the Application

The exact run command will be added once the application's entry point has been implemented.

---

## 📌 Project Status

**🚧 In Development**

Current focus:

Project setup
Maven configuration
PostgreSQL database setup
Database schema
Repository layer
Service layer
API / CLI layer
User registration
User authentication
Balance management
Deposits
Withdrawals
Transfers
Transaction history
System logging
JUnit 5 testing
Error handling
Final integration testing

---

## 🎓 Project Goals

This project is intended to demonstrate practical experience with:

* Object-oriented Java development
* Layered application architecture
* JDBC and SQL
* PostgreSQL database integration
* Maven project management
* Unit testing with JUnit 5
* Exception and error handling
* Database transactions
* Logging
* Git version control
* Agile development practices
