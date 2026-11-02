# Java Expense Tracker

A desktop-based Expense Tracker application developed using **Java Swing** and **MySQL**. It helps users manage personal finances by tracking expenses, income, budgets, recurring payments, and financial goals.

## Features

- **User Authentication** – User registration, login, and password hashing.
- **Expense Management** – Add, edit, delete, search, and categorize expenses.
- **Income Management** – Track and manage income sources.
- **Financial Dashboard** – View total income, expenses, current balance, and monthly savings.
- **Budget Management** – Set monthly budgets and monitor spending.
- **Recurring Expenses** – Manage daily, weekly, and monthly recurring payments.
- **Financial Goals** – Set savings goals and monitor progress.
- **Transaction History** – View and filter income and expense transactions.
- **Monthly Financial Reports** – Review monthly financial summaries.
- **Data Export** – Export financial information to CSV and PDF.

## Technologies Used

- Java 21
- Java Swing
- MySQL
- JDBC
- Maven
- iText PDF Library
- IntelliJ IDEA

## Database

Database name: `expense_tracker`

Main tables:

- users
- expenses
- income
- budgets
- recurring_expenses
- financial_goals

## Requirements

- JDK 21 or later
- MySQL Server
- IntelliJ IDEA
- Maven

## Setup Instructions

1. Clone or download this repository.
2. Open the project in IntelliJ IDEA.
3. Create a MySQL database named `expense_tracker`.
4. Create the required database tables.
5. Configure the MySQL connection in `DatabaseConnection.java`.
6. Ensure Maven dependencies are installed.
7. Run the application using `Main.java`.

## Database Configuration

Update the database connection settings in `DatabaseConnection.java` to match your local MySQL configuration.

**Important:** Do not upload real database passwords or private credentials to GitHub.

## Author

**M. I. F. Rushdha**

B.Tech in Software Technology

University of Vocational Technology