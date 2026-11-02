
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class RecurringExpenseGUI extends JFrame {

    private DefaultTableModel tableModel;
    private JTable recurringTable;

    public RecurringExpenseGUI() {

        setTitle("Recurring Expense Management");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Heading
        JLabel heading = new JLabel(
                "Recurring Expense Management",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 24));
        add(heading, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "ID",
                "Description",
                "Amount (LKR)",
                "Category",
                "Frequency",
                "Next Payment Date",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recurringTable = new JTable(tableModel);
        recurringTable.setRowHeight(28);
        recurringTable.setFont(new Font("Arial", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(recurringTable);

        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(
                new GridLayout(1, 6, 8, 8)
        );

        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton statusButton = new JButton("Change Status");
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(statusButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        addButton.addActionListener(e -> addRecurringExpense());

        editButton.addActionListener(e -> editRecurringExpense());

        deleteButton.addActionListener(e -> deleteRecurringExpense());

        statusButton.addActionListener(e -> changeStatus());

        refreshButton.addActionListener(e -> refreshTable());

        closeButton.addActionListener(e -> dispose());

        refreshTable();
    }

    // =========================================
    // REFRESH TABLE
    // =========================================

    private void refreshTable() {

        tableModel.setRowCount(0);

        for (RecurringExpense recurring : Main.recurringExpenses) {

            tableModel.addRow(new Object[]{

                    recurring.getId(),
                    recurring.getDescription(),
                    String.format("%.2f", recurring.getAmount()),
                    recurring.getCategory(),
                    recurring.getFrequency(),
                    recurring.getNextPaymentDate(),
                    recurring.isActive() ? "Active" : "Inactive"
            });
        }
    }

    // =========================================
    // ADD RECURRING EXPENSE
    // =========================================

    private void addRecurringExpense() {

        JTextField descriptionField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField categoryField = new JTextField();

        JComboBox<String> frequencyCombo = new JComboBox<>(
                new String[]{"Daily", "Weekly", "Monthly"}
        );

        JTextField dateField = new JTextField(
                LocalDate.now().toString()
        );

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);

        panel.add(new JLabel("Amount (LKR):"));
        panel.add(amountField);

        panel.add(new JLabel("Category:"));
        panel.add(categoryField);

        panel.add(new JLabel("Frequency:"));
        panel.add(frequencyCombo);

        panel.add(new JLabel("Next Payment Date (YYYY-MM-DD):"));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Recurring Expense",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String description = descriptionField.getText().trim();
            String category = categoryField.getText().trim();

            if (description.isEmpty() || category.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter description and category."
                );

                return;
            }

            double amount = Double.parseDouble(
                    amountField.getText().trim()
            );

            if (!Double.isFinite(amount) || amount <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Amount must be greater than zero."
                );

                return;
            }

            String frequency =
                    frequencyCombo.getSelectedItem().toString();

            LocalDate nextDate =
                    LocalDate.parse(dateField.getText().trim());

            RecurringExpense recurring = new RecurringExpense(
                    Main.nextRecurringId,
                    description,
                    amount,
                    category,
                    frequency,
                    nextDate,
                    true,
                    null
            );

            Main.recurringExpenses.add(recurring);
            Main.nextRecurringId++;

            Main.saveRecurringExpenses();

            refreshTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Recurring expense added successfully!"
            );

        } catch (NumberFormatException | DateTimeParseException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid amount and date.\n" +
                            "Date format: YYYY-MM-DD",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================
    // EDIT RECURRING EXPENSE
    // =========================================

    private void editRecurringExpense() {

        int selectedRow = recurringTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a recurring expense to edit."
            );

            return;
        }

        int modelRow = recurringTable.convertRowIndexToModel(selectedRow);

        int id = (int) tableModel.getValueAt(modelRow, 0);

        RecurringExpense recurring = findRecurringExpense(id);

        if (recurring == null) {
            return;
        }

        JTextField descriptionField =
                new JTextField(recurring.getDescription());

        JTextField amountField =
                new JTextField(String.valueOf(recurring.getAmount()));

        JTextField categoryField =
                new JTextField(recurring.getCategory());

        JComboBox<String> frequencyCombo = new JComboBox<>(
                new String[]{"Daily", "Weekly", "Monthly"}
        );

        frequencyCombo.setSelectedItem(recurring.getFrequency());

        JTextField dateField =
                new JTextField(recurring.getNextPaymentDate().toString());

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);

        panel.add(new JLabel("Amount (LKR):"));
        panel.add(amountField);

        panel.add(new JLabel("Category:"));
        panel.add(categoryField);

        panel.add(new JLabel("Frequency:"));
        panel.add(frequencyCombo);

        panel.add(new JLabel("Next Payment Date:"));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Recurring Expense",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String description = descriptionField.getText().trim();
            String category = categoryField.getText().trim();

            if (description.isEmpty() || category.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Description and category cannot be empty."
                );

                return;
            }

            double amount = Double.parseDouble(
                    amountField.getText().trim()
            );

            if (!Double.isFinite(amount) || amount <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Amount must be greater than zero."
                );

                return;
            }

            LocalDate nextDate =
                    LocalDate.parse(dateField.getText().trim());

            recurring.setDescription(description);
            recurring.setAmount(amount);
            recurring.setCategory(category);

            recurring.setFrequency(
                    frequencyCombo.getSelectedItem().toString()
            );

            recurring.setNextPaymentDate(nextDate);

            Main.saveRecurringExpenses();

            refreshTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Recurring expense updated successfully!"
            );

        } catch (NumberFormatException | DateTimeParseException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid amount and date."
            );
        }
    }

    // =========================================
    // DELETE RECURRING EXPENSE
    // =========================================

    private void deleteRecurringExpense() {

        int selectedRow = recurringTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a recurring expense to delete."
            );

            return;
        }

        int modelRow = recurringTable.convertRowIndexToModel(selectedRow);

        int id = (int) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this recurring expense?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        RecurringExpense recurring = findRecurringExpense(id);

        if (recurring != null) {

            Main.recurringExpenses.remove(recurring);

            Main.saveRecurringExpenses();

            refreshTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Recurring expense deleted successfully!"
            );
        }
    }

    // =========================================
    // CHANGE ACTIVE / INACTIVE STATUS
    // =========================================

    private void changeStatus() {

        int selectedRow = recurringTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a recurring expense."
            );

            return;
        }

        int modelRow = recurringTable.convertRowIndexToModel(selectedRow);

        int id = (int) tableModel.getValueAt(modelRow, 0);

        RecurringExpense recurring = findRecurringExpense(id);

        if (recurring != null) {

            recurring.setActive(!recurring.isActive());

            Main.saveRecurringExpenses();

            refreshTable();
        }
    }

    // =========================================
    // FIND RECURRING EXPENSE
    // =========================================

    private RecurringExpense findRecurringExpense(int id) {

        for (RecurringExpense recurring : Main.recurringExpenses) {

            if (recurring.getId() == id) {
                return recurring;
            }
        }

        return null;
    }
}

