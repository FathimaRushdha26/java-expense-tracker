
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class FinancialGoalGUI extends JFrame {

    private DefaultTableModel model;
    private JTable table;

    public FinancialGoalGUI() {

        setTitle("Financial Goals Management");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "Financial Goals Management",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 24));
        add(heading, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{
                        "ID", "Goal Name", "Target (LKR)",
                        "Saved (LKR)", "Remaining (LKR)",
                        "Target Date", "Progress", "Status"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Add Goal");
        JButton editButton = new JButton("Edit Goal");
        JButton deleteButton = new JButton("Delete Goal");
        JButton updateButton = new JButton("Update Savings");
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");

        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        buttons.add(updateButton);
        buttons.add(refreshButton);
        buttons.add(closeButton);

        add(buttons, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addGoal());
        editButton.addActionListener(e -> editGoal());
        deleteButton.addActionListener(e -> deleteGoal());
        updateButton.addActionListener(e -> updateSavings());
        refreshButton.addActionListener(e -> refreshTable());
        closeButton.addActionListener(e -> dispose());

        refreshTable();
    }

    private void refreshTable() {

        model.setRowCount(0);

        for (FinancialGoal goal : Main.financialGoals) {

            String status = goal.isCompleted()
                    ? "Completed" : "In Progress";

            model.addRow(new Object[]{
                    goal.getId(),
                    goal.getGoalName(),
                    String.format("%.2f", goal.getTargetAmount()),
                    String.format("%.2f", goal.getCurrentAmount()),
                    String.format("%.2f", goal.getRemainingAmount()),
                    goal.getTargetDate() == null
                            ? "Not set" : goal.getTargetDate(),
                    String.format("%.1f%%", goal.getProgress()),
                    status
            });
        }
    }

    private FinancialGoal findGoal(int id) {

        for (FinancialGoal goal : Main.financialGoals) {

            if (goal.getId() == id) {
                return goal;
            }
        }

        return null;
    }

    private int getSelectedGoalId() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a goal first."
            );

            return -1;
        }

        return Integer.parseInt(
                model.getValueAt(row, 0).toString()
        );
    }

    private void addGoal() {

        JTextField nameField = new JTextField();
        JTextField targetField = new JTextField();
        JTextField savedField = new JTextField("0");
        JTextField dateField = new JTextField();

        Object[] fields = {
                "Goal Name:", nameField,
                "Target Amount (LKR):", targetField,
                "Current Savings (LKR):", savedField,
                "Target Date (YYYY-MM-DD, optional):", dateField
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Add Financial Goal",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String name = nameField.getText().trim();

            double target = Double.parseDouble(
                    targetField.getText().trim()
            );

            double saved = Double.parseDouble(
                    savedField.getText().trim()
            );

            String dateText = dateField.getText().trim();

            LocalDate targetDate = dateText.isEmpty()
                    ? null : LocalDate.parse(dateText);

            if (name.isEmpty() || !Double.isFinite(target)
                    || !Double.isFinite(saved)
                    || target <= 0 || saved < 0) {

                throw new IllegalArgumentException(
                        "Enter valid goal details."
                );
            }

            FinancialGoal goal = new FinancialGoal(
                    Main.nextGoalId,
                    name,
                    target,
                    saved,
                    targetDate,
                    saved >= target
            );

            Main.financialGoals.add(goal);
            Main.nextGoalId++;

            Main.saveFinancialGoals();
            refreshTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Financial goal added successfully!"
            );

        } catch (NumberFormatException | DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid amount and date (YYYY-MM-DD)."
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage()
            );
        }
    }

    private void editGoal() {

        int id = getSelectedGoalId();

        if (id == -1) {
            return;
        }

        FinancialGoal goal = findGoal(id);

        if (goal == null) {
            return;
        }

        JTextField nameField = new JTextField(goal.getGoalName());
        JTextField targetField = new JTextField(
                String.valueOf(goal.getTargetAmount())
        );

        JTextField dateField = new JTextField(
                goal.getTargetDate() == null
                        ? "" : goal.getTargetDate().toString()
        );

        Object[] fields = {
                "Goal Name:", nameField,
                "Target Amount:", targetField,
                "Target Date (YYYY-MM-DD, optional):", dateField
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Edit Financial Goal",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String name = nameField.getText().trim();
            double target = Double.parseDouble(
                    targetField.getText().trim()
            );

            String dateText = dateField.getText().trim();

            LocalDate date = dateText.isEmpty()
                    ? null : LocalDate.parse(dateText);

            if (name.isEmpty() || !Double.isFinite(target)
                    || target <= 0) {

                throw new IllegalArgumentException(
                        "Enter valid goal details."
                );
            }

            goal.setGoalName(name);
            goal.setTargetAmount(target);
            goal.setTargetDate(date);
            goal.setCurrentAmount(goal.getCurrentAmount());

            Main.saveFinancialGoals();
            refreshTable();

        } catch (NumberFormatException | DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid amount and date."
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage()
            );
        }
    }

    private void updateSavings() {

        int id = getSelectedGoalId();

        if (id == -1) {
            return;
        }

        FinancialGoal goal = findGoal(id);

        if (goal == null) {
            return;
        }

        String input = JOptionPane.showInputDialog(
                this,
                "Enter new total savings amount (LKR):",
                goal.getCurrentAmount()
        );

        if (input == null) {
            return;
        }

        try {

            double amount = Double.parseDouble(input.trim());

            if (!Double.isFinite(amount) || amount < 0) {
                throw new IllegalArgumentException(
                        "Savings cannot be negative."
                );
            }

            goal.setCurrentAmount(amount);

            Main.saveFinancialGoals();
            refreshTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Savings updated successfully!"
            );


        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid savings amount."
            );
        }
    }

    private void deleteGoal() {

        int id = getSelectedGoalId();

        if (id == -1) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this goal?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            FinancialGoal goal = findGoal(id);

            if (goal != null) {
                Main.financialGoals.remove(goal);
                Main.saveFinancialGoals();
                refreshTable();
            }
        }
    }
}

