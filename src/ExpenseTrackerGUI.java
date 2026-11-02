
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.TreeMap;

public class ExpenseTrackerGUI extends JFrame {

    // Dashboard instance for automatic refresh
    private static ExpenseTrackerGUI dashboardInstance;

    private JTable table;
    private DefaultTableModel model;

    private JLabel totalLabel;
    private JLabel budgetLabel;
    private JLabel remainingLabel;

    private JLabel incomeValue;
    private JLabel expenseValue;
    private JLabel balanceValue;
    private JLabel savingsValue;

    public ExpenseTrackerGUI() {

        setTitle("Expense Tracker - Personal Finance Manager");
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Register this dashboard window
        dashboardInstance = this;

        createGUI();
        refreshTable();
    }

    // Automatically refresh dashboard from other windows
    public static void refreshDashboard() {

        if (dashboardInstance != null) {
            dashboardInstance.refreshTable();
        }
    }

    private void createGUI() {

        setLayout(new BorderLayout(10, 10));

        // Heading
        JLabel heading = new JLabel(
                "PERSONAL EXPENSE TRACKER",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setBorder(
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        );

        add(heading, BorderLayout.NORTH);

        // Dashboard
        JPanel dashboard = new JPanel(
                new GridLayout(2, 4, 10, 10)
        );

        dashboard.setBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );

        // Financial dashboard values
        incomeValue = new JLabel("Rs. 0.00");
        expenseValue = new JLabel("Rs. 0.00");
        balanceValue = new JLabel("Rs. 0.00");
        savingsValue = new JLabel("Rs. 0.00");

        dashboard.add(createFinancialCard(
                "TOTAL INCOME", incomeValue
        ));

        dashboard.add(createFinancialCard(
                "TOTAL EXPENSES", expenseValue
        ));

        dashboard.add(createFinancialCard(
                "CURRENT BALANCE", balanceValue
        ));

        dashboard.add(createFinancialCard(
                "MONTHLY SAVINGS", savingsValue
        ));

        // Existing budget dashboard
        totalLabel = new JLabel("Total Expenses: Rs. 0.00");
        budgetLabel = new JLabel("Monthly Budget: Rs. 0.00");
        remainingLabel = new JLabel("Remaining: Rs. 0.00");

        dashboard.add(createCard(totalLabel));
        dashboard.add(createCard(budgetLabel));
        dashboard.add(createCard(remainingLabel));

        // Expense table
        String[] columns = {
                "ID", "Description", "Amount", "Category", "Date"
        };

        model = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(table);

        // Buttons panel
        JPanel buttons = new JPanel(
                new GridLayout(4, 4, 8, 8)
        );

        JButton addButton = new JButton("Add Expense");
        JButton editButton = new JButton("Edit Expense");
        JButton deleteButton = new JButton("Delete Expense");
        JButton budgetButton = new JButton("Set Budget");
        JButton summaryButton = new JButton("Monthly Summary");
        JButton categoryButton = new JButton("Category Summary");

        JButton chartButton = new JButton("Expense Chart");
        JButton exportButton = new JButton("Export Expenses");
        JButton pdfButton = new JButton("Export PDF");
        JButton refreshButton = new JButton("Refresh");
        JButton logoutButton = new JButton("Logout");
        JButton incomeButton = new JButton("Income Management");
        JButton historyButton = new JButton("Transaction History");
        JButton monthlyReportButton = new JButton("Monthly Report");
        JButton recurringButton = new JButton("Recurring Expenses");

        JButton goalsButton = new JButton("Financial Goals");
        buttons.add(goalsButton);

        goalsButton.addActionListener(e ->
                new FinancialGoalGUI().setVisible(true));


        // Add buttons
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        buttons.add(budgetButton);
        buttons.add(summaryButton);
        buttons.add(categoryButton);

        buttons.add(chartButton);
        buttons.add(exportButton);
        buttons.add(pdfButton);
        buttons.add(refreshButton);
        buttons.add(logoutButton);
        buttons.add(incomeButton);
        buttons.add(historyButton);
        buttons.add(monthlyReportButton);
        buttons.add(recurringButton);

        // Button actions
        addButton.addActionListener(e -> addExpense());
        editButton.addActionListener(e -> editExpense());
        deleteButton.addActionListener(e -> deleteExpense());
        budgetButton.addActionListener(e -> setBudget());
        summaryButton.addActionListener(e -> monthlySummary());
        categoryButton.addActionListener(e -> categorySummary());
        monthlyReportButton.addActionListener(e ->
                new MonthlyReportGUI().setVisible(true));
        chartButton.addActionListener(e ->
                new ExpenseChartWindow().setVisible(true));
        recurringButton.addActionListener(e ->
                new RecurringExpenseGUI().setVisible(true));

        exportButton.addActionListener(e -> exportExpenses());
        pdfButton.addActionListener(e -> exportPDF());
        refreshButton.addActionListener(e -> refreshTable());
        historyButton.addActionListener(e ->
                new TransactionHistoryGUI().setVisible(true));

        incomeButton.addActionListener(e ->
                new IncomeManagerGUI().setVisible(true));

        logoutButton.addActionListener(e -> {

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {

                dashboardInstance = null;
                dispose();

                SwingUtilities.invokeLater(() ->
                        new LoginWindow().setVisible(true));
            }
        });

        // Center panel
        JPanel centerPanel = new JPanel(
                new BorderLayout(10, 10)
        );

        centerPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );

        centerPanel.add(dashboard, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttons, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    // Financial dashboard card
    private JPanel createFinancialCard(
            String title,
            JLabel valueLabel
    ) {

        JPanel panel = new JPanel(
                new GridLayout(2, 1, 5, 5)
        );

        JLabel titleLabel = new JLabel(
                title,
                SwingConstants.CENTER
        );

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        valueLabel.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        valueLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        panel.add(titleLabel);
        panel.add(valueLabel);

        panel.setBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1)
        );

        return panel;
    }

    // Existing dashboard card
    private JPanel createCard(JLabel label) {

        JPanel panel = new JPanel(new GridBagLayout());

        label.setFont(
                new Font("Arial", Font.BOLD, 15)
        );

        panel.add(label);

        panel.setBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1)
        );

        return panel;
    }

    // Add expense
    private void addExpense() {

        JTextField descriptionField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField dateField = new JTextField(
                LocalDate.now().toString()
        );

        Object[] fields = {
                "Description:", descriptionField,
                "Amount:", amountField,
                "Category:", categoryField,
                "Date (YYYY-MM-DD):", dateField
        };

        int result = JOptionPane.showConfirmDialog(
                this, fields, "Add New Expense",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String description = descriptionField.getText().trim();
            double amount = Double.parseDouble(
                    amountField.getText().trim()
            );

            String category = categoryField.getText().trim();
            String date = dateField.getText().trim();

            LocalDate.parse(date);

            if (description.isEmpty() || category.isEmpty()
                    || amount <= 0 || !Double.isFinite(amount)) {
                throw new IllegalArgumentException();
            }

            Expense expense = new Expense(
                    Main.nextId++, description, amount, category, date
            );

            Main.expenses.add(expense);
            Main.saveExpenses();
            refreshTable();

            JOptionPane.showMessageDialog(
                    this, "Expense added successfully!"
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this, "Enter a valid amount!"
            );

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this, "Enter a valid date (YYYY-MM-DD)!"
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this, "Please enter valid details!"
            );
        }
    }

    // Edit expense
    private void editExpense() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an expense first!"
            );
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        Expense selected = findExpense(id);

        if (selected == null) {
            return;
        }

        JTextField descriptionField =
                new JTextField(selected.getDescription());

        JTextField amountField =
                new JTextField(String.valueOf(selected.getAmount()));

        JTextField categoryField =
                new JTextField(selected.getCategory());

        JTextField dateField =
                new JTextField(selected.getDate());

        Object[] fields = {
                "Description:", descriptionField,
                "Amount:", amountField,
                "Category:", categoryField,
                "Date (YYYY-MM-DD):", dateField
        };

        int result = JOptionPane.showConfirmDialog(
                this, fields, "Edit Expense",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String description = descriptionField.getText().trim();
            double amount = Double.parseDouble(
                    amountField.getText().trim()
            );

            String category = categoryField.getText().trim();
            String date = dateField.getText().trim();

            LocalDate.parse(date);

            if (description.isEmpty() || category.isEmpty()
                    || amount <= 0 || !Double.isFinite(amount)) {
                throw new IllegalArgumentException();
            }

            selected.setDescription(description);
            selected.setAmount(amount);
            selected.setCategory(category);
            selected.setDate(date);

            Main.saveExpenses();
            refreshTable();

            JOptionPane.showMessageDialog(
                    this, "Expense updated successfully!"
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this, "Invalid amount!"
            );

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this, "Invalid date!"
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this, "Please enter valid details!"
            );
        }
    }

    // Delete expense
    private void deleteExpense() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an expense first!"
            );
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this expense?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            Expense selected = findExpense(id);

            if (selected != null) {

                Main.expenses.remove(selected);
                Main.saveExpenses();
                refreshTable();

                JOptionPane.showMessageDialog(
                        this, "Expense deleted successfully!"
                );
            }
        }
    }

    // Set monthly budget
    private void setBudget() {

        String month = JOptionPane.showInputDialog(
                this, "Enter month (YYYY-MM):",
                YearMonth.now().toString()
        );

        if (month == null) {
            return;
        }

        try {

            month = month.trim();
            YearMonth.parse(month);

            String amountText = JOptionPane.showInputDialog(
                    this, "Enter monthly budget amount:"
            );

            if (amountText == null) {
                return;
            }

            double amount = Double.parseDouble(amountText);

            if (amount <= 0 || !Double.isFinite(amount)) {
                throw new IllegalArgumentException();
            }

            Main.budgets.put(month, amount);
            Main.saveBudgets();
            refreshTable();

            JOptionPane.showMessageDialog(
                    this, "Monthly budget saved!"
            );

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this, "Invalid month!"
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this, "Invalid amount!"
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this, "Budget must be greater than zero!"
            );
        }
    }

    // Monthly summary
    private void monthlySummary() {

        String month = JOptionPane.showInputDialog(
                this, "Enter month (YYYY-MM):",
                YearMonth.now().toString()
        );

        if (month == null) {
            return;
        }

        try {

            month = month.trim();
            YearMonth selectedMonth = YearMonth.parse(month);

            double total = Main.getMonthlyTotal(selectedMonth);
            Double budget = Main.budgets.get(month);

            String message = "Month: " + month
                    + "\nTotal Expenses: Rs. " + total;

            if (budget != null) {

                double remaining = budget - total;

                message += "\nBudget: Rs. " + budget
                        + "\nRemaining: Rs. " + remaining;

                if (remaining < 0) {
                    message += "\nWARNING: Budget exceeded!";
                }

            } else {

                message += "\nNo budget set for this month.";
            }

            JOptionPane.showMessageDialog(
                    this, message, "Monthly Summary",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this, "Invalid month!"
            );
        }
    }

    // Category summary
    private void categorySummary() {

        TreeMap<String, Double> totals = new TreeMap<>(
                String.CASE_INSENSITIVE_ORDER
        );

        for (Expense expense : Main.expenses) {

            String category = expense.getCategory();

            totals.put(
                    category,
                    totals.getOrDefault(category, 0.0)
                            + expense.getAmount()
            );
        }

        StringBuilder result = new StringBuilder(
                "===== CATEGORY SUMMARY =====\n\n"
        );

        for (String category : totals.keySet()) {

            result.append(category)
                    .append(" : Rs. ")
                    .append(totals.get(category))
                    .append("\n");
        }

        if (totals.isEmpty()) {
            result.append("No expenses available.");
        }

        JOptionPane.showMessageDialog(this, result.toString());
    }

    // Export expenses as CSV
    private void exportExpenses() {

        if (Main.expenses.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "No expenses available to export!"
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle("Save Expense Report");
        chooser.setSelectedFile(
                new File("My_Expenses.csv")
        );

        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            String filePath = chooser.getSelectedFile()
                    .getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }

            boolean success = ExpenseExporter.exportExpenses(
                    Main.expenses, filePath
            );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Expenses exported successfully!\n\nSaved at:\n"
                                + filePath
                );

            } else {

                JOptionPane.showMessageDialog(
                        this, "Export failed! Please try again."
                );
            }
        }
    }

    // Export expenses as PDF
    private void exportPDF() {

        if (Main.expenses.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "No expenses available to export!"
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle("Save PDF Expense Report");
        chooser.setSelectedFile(
                new File("Expense_Report.pdf")
        );

        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            String filePath = chooser.getSelectedFile()
                    .getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            boolean success = PDFExporter.exportReport(
                    filePath, Main.currentUsername
            );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "PDF Report Created Successfully!\n\nSaved at:\n"
                                + filePath
                );

            } else {

                JOptionPane.showMessageDialog(
                        this, "PDF Export Failed! Please try again."
                );
            }
        }
    }

    // Refresh table and dashboard
    private void refreshTable() {

        model.setRowCount(0);

        double total = 0;

        for (Expense expense : Main.expenses) {

            model.addRow(new Object[]{
                    expense.getId(),
                    expense.getDescription(),
                    expense.getAmount(),
                    expense.getCategory(),
                    expense.getDate()
            });

            total += expense.getAmount();
        }

        YearMonth currentMonth = YearMonth.now();

        double currentMonthTotal =
                Main.getMonthlyTotal(currentMonth);

        double budget = Main.budgets.getOrDefault(
                currentMonth.toString(), 0.0
        );

        totalLabel.setText(
                "Total Expenses: Rs. " + String.format("%.2f", total)
        );

        budgetLabel.setText(
                "Monthly Budget: Rs. " + String.format("%.2f", budget)
        );

        remainingLabel.setText(
                "Remaining: Rs. "
                        + String.format("%.2f", budget - currentMonthTotal)
        );

        updateDashboard();
    }

    // Update financial dashboard values
    private void updateDashboard() {

        double income = Main.getTotalIncome();
        double expenses = Main.getTotalExpenses();
        double balance = income - expenses;

        double monthlySavings =
                Main.getMonthlyBalance(YearMonth.now());

        incomeValue.setText(
                String.format("Rs. %.2f", income)
        );

        expenseValue.setText(
                String.format("Rs. %.2f", expenses)
        );

        balanceValue.setText(
                String.format("Rs. %.2f", balance)
        );

        savingsValue.setText(
                String.format("Rs. %.2f", monthlySavings)
        );
    }

    // Find expense using ID
    private Expense findExpense(int id) {

        for (Expense expense : Main.expenses) {

            if (expense.getId() == id) {
                return expense;
            }
        }

        return null;
    }
}
