
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.regex.Pattern;

public class TransactionHistoryGUI extends JFrame {

    private DefaultTableModel tableModel;
    private JTable transactionTable;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField searchField;
    private JComboBox<String> filterCombo;

    public TransactionHistoryGUI() {

        setTitle("Transaction History");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        // Heading
        JLabel heading = new JLabel(
                "Transaction History",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 24));
        add(heading, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "Type",
                "Description",
                "Amount (LKR)",
                "Category",
                "Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);

        transactionTable.setRowHeight(28);
        transactionTable.setFont(new Font("Arial", Font.PLAIN, 14));

        sorter = new TableRowSorter<>(tableModel);
        transactionTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(transactionTable);

        add(scrollPane, BorderLayout.CENTER);

        // Search and Filter Panel
        JPanel topPanel = new JPanel(new FlowLayout());

        searchField = new JTextField(20);

        filterCombo = new JComboBox<>(
                new String[]{"All", "Income", "Expense"}
        );

        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);

        topPanel.add(new JLabel("Filter:"));
        topPanel.add(filterCombo);

        add(topPanel, BorderLayout.PAGE_START);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();

        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");

        bottomPanel.add(refreshButton);
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Events
        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        applyFilters();
                    }

                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        applyFilters();
                    }

                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        applyFilters();
                    }
                }
        );

        filterCombo.addActionListener(e -> applyFilters());

        refreshButton.addActionListener(e -> loadTransactions());

        closeButton.addActionListener(e -> dispose());

        loadTransactions();
    }

    private void loadTransactions() {

        tableModel.setRowCount(0);

        // Load Expenses
        for (Expense expense : Main.expenses) {

            tableModel.addRow(new Object[]{
                    "Expense",
                    expense.getDescription(),
                    expense.getAmount(),
                    expense.getCategory(),
                    expense.getDate()
            });
        }

        // Load Incomes
        for (Income income : Main.incomes) {

            tableModel.addRow(new Object[]{
                    "Income",
                    income.getDescription(),
                    income.getAmount(),
                    income.getCategory(),
                    income.getDate()
            });
        }

        // Sort by Date (newest first)
        sorter.setSortKeys(
                java.util.List.of(
                        new javax.swing.RowSorter.SortKey(
                                4,
                                javax.swing.SortOrder.DESCENDING
                        )
                )
        );

        applyFilters();
    }

    private void applyFilters() {

        String searchText = searchField.getText().trim();

        String selectedType =
                filterCombo.getSelectedItem().toString();

        java.util.List<RowFilter<Object, Object>> filters =
                new java.util.ArrayList<>();

        // Search Description and Category
        if (!searchText.isEmpty()) {

            filters.add(
                    RowFilter.regexFilter(
                            "(?i)" + Pattern.quote(searchText),
                            1, 3
                    )
            );
        }

        // Filter Income or Expense
        if (!selectedType.equals("All")) {

            filters.add(
                    RowFilter.regexFilter(
                            "^" + Pattern.quote(selectedType) + "$",
                            0
                    )
            );
        }

        sorter.setRowFilter(
                filters.isEmpty()
                        ? null
                        : RowFilter.andFilter(filters)
        );
    }
}

