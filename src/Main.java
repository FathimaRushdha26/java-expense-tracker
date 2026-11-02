
import javax.swing.*;
import java.sql.*;
import java.time.YearMonth;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    // =========================================
    // EXPENSE, INCOME, BUDGET AND RECURRING DATA
    // =========================================

    public static ArrayList<Expense> expenses = new ArrayList<>();
    public static ArrayList<Income> incomes = new ArrayList<>();
    public static HashMap<String, Double> budgets = new HashMap<>();
    public static ArrayList<RecurringExpense> recurringExpenses = new ArrayList<>();
    public static ArrayList<FinancialGoal> financialGoals = new ArrayList<>();

    // IDs
    public static int nextId = 1;
    public static int nextIncomeId = 1;
    public static int nextRecurringId = 1;
    public static int nextGoalId = 1;

    // Current logged-in user
    public static int currentUserId = -1;
    public static String currentUsername = "";

    // =========================================
    // MAIN METHOD
    // =========================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginWindow().setVisible(true);
        });
    }

    // =========================================
    // START USER SESSION
    // =========================================

    public static void startUserSession(String username) {

        currentUsername = username;
        currentUserId = UserAuth.getUserId(username);

        if (currentUserId == -1) {
            JOptionPane.showMessageDialog(
                    null,
                    "Unable to find user in database."
            );
            return;
        }

        expenses.clear();
        incomes.clear();
        budgets.clear();
        recurringExpenses.clear();
        financialGoals.clear();

        loadExpenses();
        loadIncomes();
        loadBudgets();
        loadRecurringExpenses();
        loadFinancialGoals();

        // Automatically generate due recurring expenses
        processDueRecurringExpenses();

        SwingUtilities.invokeLater(() -> {
            new ExpenseTrackerGUI().setVisible(true);
        });
    }

    // =========================================
    // EXPENSE MANAGEMENT
    // =========================================

    public static void loadExpenses() {

        String sql = """
                SELECT id, description, amount, category, expense_date
                FROM expenses
                WHERE user_id = ?
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(
                         "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM expenses")) {

                if (result.next()) {
                    nextId = result.getInt("next_id");
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, currentUserId);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        Expense expense = new Expense(
                                result.getInt("id"),
                                result.getString("description"),
                                result.getDouble("amount"),
                                result.getString("category"),
                                result.getDate("expense_date").toString()
                        );

                        expenses.add(expense);
                    }
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    public static void saveExpenses() {

        String deleteSQL = "DELETE FROM expenses WHERE user_id = ?";

        String insertSQL = """
                INSERT INTO expenses
                (id, user_id, description, amount, category, expense_date)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                try (PreparedStatement deleteStatement =
                             connection.prepareStatement(deleteSQL)) {

                    deleteStatement.setInt(1, currentUserId);
                    deleteStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement =
                             connection.prepareStatement(insertSQL)) {

                    for (Expense expense : expenses) {

                        insertStatement.setInt(1, expense.getId());
                        insertStatement.setInt(2, currentUserId);
                        insertStatement.setString(3, expense.getDescription());
                        insertStatement.setDouble(4, expense.getAmount());
                        insertStatement.setString(5, expense.getCategory());
                        insertStatement.setDate(
                                6,
                                Date.valueOf(expense.getDate())
                        );

                        insertStatement.addBatch();
                    }

                    insertStatement.executeBatch();
                }

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    // =========================================
    // INCOME MANAGEMENT
    // =========================================

    public static void loadIncomes() {

        String sql = """
                SELECT id, description, amount, category, income_date
                FROM income
                WHERE user_id = ?
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(
                         "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM income")) {

                if (result.next()) {
                    nextIncomeId = result.getInt("next_id");
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, currentUserId);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        Income income = new Income(
                                result.getInt("id"),
                                result.getString("description"),
                                result.getDouble("amount"),
                                result.getString("category"),
                                result.getDate("income_date").toString()
                        );

                        incomes.add(income);
                    }
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    public static void saveIncomes() {

        String deleteSQL = "DELETE FROM income WHERE user_id = ?";

        String insertSQL = """
                INSERT INTO income
                (id, user_id, description, amount, category, income_date)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                try (PreparedStatement deleteStatement =
                             connection.prepareStatement(deleteSQL)) {

                    deleteStatement.setInt(1, currentUserId);
                    deleteStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement =
                             connection.prepareStatement(insertSQL)) {

                    for (Income income : incomes) {

                        insertStatement.setInt(1, income.getId());
                        insertStatement.setInt(2, currentUserId);
                        insertStatement.setString(3, income.getDescription());
                        insertStatement.setDouble(4, income.getAmount());
                        insertStatement.setString(5, income.getCategory());
                        insertStatement.setDate(
                                6,
                                Date.valueOf(income.getDate())
                        );

                        insertStatement.addBatch();
                    }

                    insertStatement.executeBatch();
                }

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    public static double getMonthlyIncome(YearMonth month) {

        double total = 0;

        for (Income income : incomes) {

            YearMonth incomeMonth =
                    YearMonth.parse(income.getDate().substring(0, 7));

            if (incomeMonth.equals(month)) {
                total += income.getAmount();
            }
        }

        return total;
    }

    public static double getTotalIncome() {

        double total = 0;

        for (Income income : incomes) {
            total += income.getAmount();
        }

        return total;
    }

    // =========================================
    // BUDGET MANAGEMENT
    // =========================================

    public static void loadBudgets() {

        String sql = """
                SELECT budget_month, amount
                FROM budgets
                WHERE user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, currentUserId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    budgets.put(
                            result.getString("budget_month"),
                            result.getDouble("amount")
                    );
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    public static void saveBudgets() {

        String deleteSQL = "DELETE FROM budgets WHERE user_id = ?";

        String insertSQL = """
                INSERT INTO budgets (user_id, budget_month, amount)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                try (PreparedStatement deleteStatement =
                             connection.prepareStatement(deleteSQL)) {

                    deleteStatement.setInt(1, currentUserId);
                    deleteStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement =
                             connection.prepareStatement(insertSQL)) {

                    for (String month : budgets.keySet()) {

                        insertStatement.setInt(1, currentUserId);
                        insertStatement.setString(2, month);
                        insertStatement.setDouble(3, budgets.get(month));

                        insertStatement.addBatch();
                    }

                    insertStatement.executeBatch();
                }

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    // =========================================
    // RECURRING EXPENSE MANAGEMENT
    // =========================================

    public static void loadRecurringExpenses() {

        recurringExpenses.clear();
        nextRecurringId = 1;

        String sql = """
                SELECT id, description, amount, category, frequency,
                       next_payment_date, is_active, last_generated_date
                FROM recurring_expenses
                WHERE user_id = ?
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(
                         "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM recurring_expenses")) {

                if (result.next()) {
                    nextRecurringId = result.getInt("next_id");
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, currentUserId);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        Date nextDate = result.getDate("next_payment_date");
                        Date lastDate = result.getDate("last_generated_date");

                        RecurringExpense recurring = new RecurringExpense(
                                result.getInt("id"),
                                result.getString("description"),
                                result.getDouble("amount"),
                                result.getString("category"),
                                result.getString("frequency"),
                                nextDate.toLocalDate(),
                                result.getBoolean("is_active"),
                                lastDate == null ? null : lastDate.toLocalDate()
                        );

                        recurringExpenses.add(recurring);
                    }
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    public static void saveRecurringExpenses() {

        String deleteSQL =
                "DELETE FROM recurring_expenses WHERE user_id = ?";

        String insertSQL = """
                INSERT INTO recurring_expenses
                (id, user_id, description, amount, category, frequency,
                 next_payment_date, is_active, last_generated_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                try (PreparedStatement deleteStatement =
                             connection.prepareStatement(deleteSQL)) {

                    deleteStatement.setInt(1, currentUserId);
                    deleteStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement =
                             connection.prepareStatement(insertSQL)) {

                    for (RecurringExpense recurring : recurringExpenses) {

                        insertStatement.setInt(1, recurring.getId());
                        insertStatement.setInt(2, currentUserId);
                        insertStatement.setString(3, recurring.getDescription());
                        insertStatement.setDouble(4, recurring.getAmount());
                        insertStatement.setString(5, recurring.getCategory());
                        insertStatement.setString(6, recurring.getFrequency());

                        insertStatement.setDate(
                                7,
                                Date.valueOf(recurring.getNextPaymentDate())
                        );

                        insertStatement.setBoolean(8, recurring.isActive());

                        if (recurring.getLastGeneratedDate() == null) {

                            insertStatement.setNull(9, Types.DATE);

                        } else {

                            insertStatement.setDate(
                                    9,
                                    Date.valueOf(recurring.getLastGeneratedDate())
                            );
                        }

                        insertStatement.addBatch();
                    }

                    insertStatement.executeBatch();
                }

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    // =========================================
    // AUTOMATIC RECURRING EXPENSE GENERATION
    // =========================================

    public static void processDueRecurringExpenses() {

        LocalDate today = LocalDate.now();

        boolean expensesChanged = false;
        boolean recurringChanged = false;

        for (RecurringExpense recurring : recurringExpenses) {

            if (!recurring.isActive()) {
                continue;
            }

            LocalDate paymentDate = recurring.getNextPaymentDate();

            if (paymentDate == null) {
                continue;
            }

            while (!paymentDate.isAfter(today)) {

                boolean alreadyExists = false;

                for (Expense expense : expenses) {

                    if (expense.getDate().equals(paymentDate.toString())
                            && expense.getDescription().equalsIgnoreCase(
                            recurring.getDescription())
                            && Double.compare(
                            expense.getAmount(),
                            recurring.getAmount()) == 0
                            && expense.getCategory().equalsIgnoreCase(
                            recurring.getCategory())) {

                        alreadyExists = true;
                        break;
                    }
                }

                if (!alreadyExists) {

                    Expense newExpense = new Expense(
                            nextId,
                            recurring.getDescription(),
                            recurring.getAmount(),
                            recurring.getCategory(),
                            paymentDate.toString()
                    );

                    expenses.add(newExpense);
                    nextId++;

                    expensesChanged = true;

                    System.out.println(
                            "Recurring expense generated: "
                                    + recurring.getDescription()
                    );
                }

                recurring.setLastGeneratedDate(paymentDate);
                recurringChanged = true;

                switch (recurring.getFrequency().toLowerCase()) {

                    case "daily":
                        paymentDate = paymentDate.plusDays(1);
                        break;

                    case "weekly":
                        paymentDate = paymentDate.plusWeeks(1);
                        break;

                    case "monthly":
                        paymentDate = paymentDate.plusMonths(1);
                        break;

                    default:
                        System.out.println(
                                "Unknown recurring frequency: "
                                        + recurring.getFrequency()
                        );
                        paymentDate = today.plusDays(1);
                        break;
                }
            }

            recurring.setNextPaymentDate(paymentDate);
        }

        if (expensesChanged) {
            saveExpenses();
        }

        if (recurringChanged) {
            saveRecurringExpenses();
        }
    }

    // =========================================
    // FINANCIAL GOALS MANAGEMENT
    // =========================================

    // Load financial goals from MySQL
    public static void loadFinancialGoals() {

        financialGoals.clear();
        nextGoalId = 1;

        String sql = """
                SELECT id, goal_name, target_amount, current_amount,
                       target_date, is_completed
                FROM financial_goals
                WHERE user_id = ?
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            // Get next available goal ID
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(
                         "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM financial_goals")) {

                if (result.next()) {
                    nextGoalId = result.getInt("next_id");
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, currentUserId);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        Date targetDate = result.getDate("target_date");

                        FinancialGoal goal = new FinancialGoal(
                                result.getInt("id"),
                                result.getString("goal_name"),
                                result.getDouble("target_amount"),
                                result.getDouble("current_amount"),
                                targetDate == null ? null : targetDate.toLocalDate(),
                                result.getBoolean("is_completed")
                        );

                        financialGoals.add(goal);
                    }
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    // Save financial goals to MySQL
    public static void saveFinancialGoals() {

        String deleteSQL =
                "DELETE FROM financial_goals WHERE user_id = ?";

        String insertSQL = """
                INSERT INTO financial_goals
                (id, user_id, goal_name, target_amount, current_amount,
                 target_date, is_completed)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Delete existing goals for current user
                try (PreparedStatement deleteStatement =
                             connection.prepareStatement(deleteSQL)) {

                    deleteStatement.setInt(1, currentUserId);
                    deleteStatement.executeUpdate();
                }

                // Insert updated goals
                try (PreparedStatement insertStatement =
                             connection.prepareStatement(insertSQL)) {

                    for (FinancialGoal goal : financialGoals) {

                        insertStatement.setInt(1, goal.getId());
                        insertStatement.setInt(2, currentUserId);
                        insertStatement.setString(3, goal.getGoalName());
                        insertStatement.setDouble(4, goal.getTargetAmount());
                        insertStatement.setDouble(5, goal.getCurrentAmount());

                        if (goal.getTargetDate() == null) {

                            insertStatement.setNull(6, Types.DATE);

                        } else {

                            insertStatement.setDate(
                                    6,
                                    Date.valueOf(goal.getTargetDate())
                            );
                        }

                        insertStatement.setBoolean(
                                7,
                                goal.isCompleted()
                        );

                        insertStatement.addBatch();
                    }

                    insertStatement.executeBatch();
                }

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    // =========================================
    // SUMMARY CALCULATIONS
    // =========================================

    public static double getMonthlyTotal(YearMonth month) {

        double total = 0;

        for (Expense expense : expenses) {

            YearMonth expenseMonth =
                    YearMonth.parse(expense.getDate().substring(0, 7));

            if (expenseMonth.equals(month)) {
                total += expense.getAmount();
            }
        }

        return total;
    }

    public static double getTotalExpenses() {

        double total = 0;

        for (Expense expense : expenses) {
            total += expense.getAmount();
        }

        return total;
    }

    public static double getMonthlyBalance(YearMonth month) {

        return getMonthlyIncome(month) - getMonthlyTotal(month);
    }

    public static double getTotalBalance() {

        return getTotalIncome() - getTotalExpenses();
    }

    // =========================================
    // DATABASE ERROR HANDLING
    // =========================================

    private static void showDatabaseError(SQLException e) {

        e.printStackTrace();

        JOptionPane.showMessageDialog(
                null,
                "Database Error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
