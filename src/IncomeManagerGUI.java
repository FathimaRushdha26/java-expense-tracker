
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class IncomeManagerGUI extends JFrame {

    private DefaultTableModel tableModel;
    private JTable incomeTable;

    public IncomeManagerGUI() {

        setTitle("Income Management");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        createGUI();
        refreshTable();
    }

    private void createGUI() {

        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                "INCOME MANAGEMENT",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        String[] columns = {
                "ID", "Description", "Amount", "Category", "Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        incomeTable = new JTable(tableModel);
        incomeTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(incomeTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 10)
        );

        JButton addButton = new JButton("Add Income");
        JButton editButton = new JButton("Edit Income");
        JButton deleteButton = new JButton("Delete Income");
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addIncome());
        editButton.addActionListener(e -> editIncome());
        deleteButton.addActionListener(e -> deleteIncome());
        refreshButton.addActionListener(e -> refreshTable());
        closeButton.addActionListener(e -> dispose());
    }

    // Add Income
    private void addIncome() {

        JTextField descriptionField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField dateField = new JTextField(LocalDate.now().toString());

        Object[] fields = {
                "Description:", descriptionField,
                "Amount:", amountField,
                "Category:", categoryField,
                "Date (YYYY-MM-DD):", dateField
        };

        int result = JOptionPane.showConfirmDialog(
                this, fields, "Add New Income",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String description = descriptionField.getText().trim();
            double amount = Double.parseDouble(amountField.getText().trim());
            String category = categoryField.getText().trim();
            LocalDate date = LocalDate.parse(dateField.getText().trim());

            if (description.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            if (!Double.isFinite(amount) || amount <= 0) {
                JOptionPane.showMessageDialog(
                        this, "Amount must be greater than zero."
                );
                return;
            }

            Income income = new Income(
                    Main.nextIncomeId++,
                    description,
                    amount,
                    category,
                    date.toString()
            );

            Main.incomes.add(income);
            Main.saveIncomes();

            refreshTable();
            ExpenseTrackerGUI.refreshDashboard();

            JOptionPane.showMessageDialog(
                    this, "Income added successfully!"
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this, "Please enter a valid amount."
            );

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                    this, "Enter date in YYYY-MM-DD format."
            );
        }
    }

    // Edit Income
    private void editIncome() {

        int selectedRow = incomeTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an income to edit."
            );
            return;
        }

        int incomeId = (int) tableModel.getValueAt(selectedRow, 0);
        Income income = findIncome(incomeId);

        if (income == null) {
            return;
        }

        JTextField descriptionField =
                new JTextField(income.getDescription());

        JTextField amountField =
                new JTextField(String.valueOf(income.getAmount()));

        JTextField categoryField =
                new JTextField(income.getCategory());

        JTextField dateField =
                new JTextField(income.getDate());

        Object[] fields = {
                "Description:", descriptionField,
                "Amount:", amountField,
                "Category:", categoryField,
                "Date (YYYY-MM-DD):", dateField
        };

        int result = JOptionPane.showConfirmDialog(
                this, fields, "Edit Income",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String description = descriptionField.getText().trim();
            double amount = Double.parseDouble(amountField.getText().trim());
            String category = categoryField.getText().trim();
            LocalDate date = LocalDate.parse(dateField.getText().trim());

            if (description.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            if (!Double.isFinite(amount) || amount <= 0) {
                JOptionPane.showMessageDialog(
                        this, "Amount must be greater than zero."
                );
                return;
            }

            income.setDescription(description);
            income.setAmount(amount);
            income.setCategory(category);
            income.setDate(date.toString());

            Main.saveIncomes();

            refreshTable();
            ExpenseTrackerGUI.refreshDashboard();

            JOptionPane.showMessageDialog(
                    this, "Income updated successfully!"
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this, "Please enter a valid amount."
            );

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                    this, "Enter date in YYYY-MM-DD format."
            );
        }
    }

    // Delete Income
    private void deleteIncome() {

        int selectedRow = incomeTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an income to delete."
            );
            return;
        }

        int incomeId = (int) tableModel.getValueAt(selectedRow, 0);

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this income?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        Income income = findIncome(incomeId);

        if (income != null) {
            Main.incomes.remove(income);
            Main.saveIncomes();

            refreshTable();
            ExpenseTrackerGUI.refreshDashboard();

            JOptionPane.showMessageDialog(
                    this, "Income deleted successfully!"
            );
        }
    }

    // Find Income by ID
    private Income findIncome(int id) {

        for (Income income : Main.incomes) {
            if (income.getId() == id) {
                return income;
            }
        }

        return null;
    }

    // Refresh Income Table
    private void refreshTable() {

        tableModel.setRowCount(0);

        for (Income income : Main.incomes) {
            tableModel.addRow(new Object[]{
                    income.getId(),
                    income.getDescription(),
                    String.format("%.2f", income.getAmount()),
                    income.getCategory(),
                    income.getDate()
            });
        }
    }
}
