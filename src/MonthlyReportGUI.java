
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.text.NumberFormat;
import java.util.Locale;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class MonthlyReportGUI extends JFrame {

    private JComboBox<String> monthCombo;

    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel savingsLabel;

    private DefaultTableModel categoryModel;
    private ChartPanel chartPanel;

    private final NumberFormat currency =
            NumberFormat.getNumberInstance(new Locale("en", "LK"));

    public MonthlyReportGUI() {

        setTitle("Monthly Financial Report");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Heading
        JLabel heading = new JLabel(
                "Monthly Financial Report",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 25));
        add(heading, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Month selection
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        selectionPanel.add(new JLabel("Select Month:"));

        monthCombo = new JComboBox<>();

        YearMonth currentMonth = YearMonth.now();

        for (int i = 0; i < 24; i++) {
            YearMonth month = currentMonth.minusMonths(i);
            monthCombo.addItem(month.toString());
        }

        selectionPanel.add(monthCombo);

        JButton refreshButton = new JButton("Generate Report");
        JButton pdfButton = new JButton("Export PDF");

        selectionPanel.add(refreshButton);
        selectionPanel.add(pdfButton);

        mainPanel.add(selectionPanel, BorderLayout.NORTH);

        // Financial summary cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        incomeLabel = new JLabel("LKR 0.00", SwingConstants.CENTER);
        expenseLabel = new JLabel("LKR 0.00", SwingConstants.CENTER);
        savingsLabel = new JLabel("LKR 0.00", SwingConstants.CENTER);

        cardsPanel.add(createCard("Total Income", incomeLabel));
        cardsPanel.add(createCard("Total Expenses", expenseLabel));
        cardsPanel.add(createCard("Monthly Savings", savingsLabel));

        // Center content
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Chart
        chartPanel = new ChartPanel();
        chartPanel.setBorder(
                BorderFactory.createTitledBorder("Income vs Expenses")
        );

        centerPanel.add(chartPanel);

        // Category table
        String[] columns = {"Category", "Total Expense (LKR)"};

        categoryModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable categoryTable = new JTable(categoryModel);
        categoryTable.setRowHeight(28);

        JScrollPane tableScroll = new JScrollPane(categoryTable);
        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Category-wise Expenses")
        );

        centerPanel.add(tableScroll);

        // Content layout
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

        contentPanel.add(cardsPanel, BorderLayout.NORTH);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel();

        JButton closeButton = new JButton("Close");

        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Button actions
        refreshButton.addActionListener(e -> generateReport());

        pdfButton.addActionListener(e -> exportPDF());

        closeButton.addActionListener(e -> dispose());

        monthCombo.addActionListener(e -> generateReport());

        generateReport();
    }

    private JPanel createCard(String title, JLabel valueLabel) {

        JPanel card = new JPanel(new BorderLayout(5, 10));

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        BorderFactory.createEmptyBorder(15, 10, 15, 10)
                )
        );

        JLabel titleLabel = new JLabel(
                title,
                SwingConstants.CENTER
        );

        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private YearMonth getSelectedMonth() {

        return YearMonth.parse(
                monthCombo.getSelectedItem().toString()
        );
    }

    private void generateReport() {

        YearMonth selectedMonth = getSelectedMonth();

        double totalIncome = Main.getMonthlyIncome(selectedMonth);
        double totalExpenses = Main.getMonthlyTotal(selectedMonth);
        double savings = totalIncome - totalExpenses;

        incomeLabel.setText("LKR " + currency.format(totalIncome));
        expenseLabel.setText("LKR " + currency.format(totalExpenses));
        savingsLabel.setText("LKR " + currency.format(savings));

        // Category-wise expense calculation
        HashMap<String, Double> categoryTotals = new HashMap<>();

        for (Expense expense : Main.expenses) {

            try {

                YearMonth expenseMonth =
                        YearMonth.from(
                                java.time.LocalDate.parse(expense.getDate())
                        );

                if (expenseMonth.equals(selectedMonth)) {

                    String category = expense.getCategory();

                    categoryTotals.put(
                            category,
                            categoryTotals.getOrDefault(category, 0.0)
                                    + expense.getAmount()
                    );
                }

            } catch (Exception ex) {
                System.out.println("Invalid expense date: " + expense.getDate());
            }
        }

        categoryModel.setRowCount(0);

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {

            categoryModel.addRow(new Object[]{
                    entry.getKey(),
                    currency.format(entry.getValue())
            });
        }

        chartPanel.setValues(totalIncome, totalExpenses);
        chartPanel.repaint();
    }

    private void exportPDF() {

        try {

            YearMonth selectedMonth = getSelectedMonth();

            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setSelectedFile(
                    new java.io.File(
                            "Monthly_Report_" + selectedMonth + ".pdf"
                    )
            );

            int result = fileChooser.showSaveDialog(this);

            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            java.io.File selectedFile = fileChooser.getSelectedFile();

            if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
                selectedFile = new java.io.File(
                        selectedFile.getAbsolutePath() + ".pdf"
                );
            }

            Document document = new Document();

            PdfWriter.getInstance(
                    document,
                    new FileOutputStream(selectedFile)
            );

            document.open();

            document.add(new Paragraph("MONTHLY FINANCIAL REPORT"));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Month: " + selectedMonth));
            document.add(new Paragraph(" "));

            double totalIncome = Main.getMonthlyIncome(selectedMonth);
            double totalExpenses = Main.getMonthlyTotal(selectedMonth);
            double savings = totalIncome - totalExpenses;

            document.add(new Paragraph(
                    "Total Income: LKR " + currency.format(totalIncome)
            ));

            document.add(new Paragraph(
                    "Total Expenses: LKR " + currency.format(totalExpenses)
            ));

            document.add(new Paragraph(
                    "Monthly Savings: LKR " + currency.format(savings)
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("CATEGORY-WISE EXPENSES"));
            document.add(new Paragraph(" "));

            PdfPTable pdfTable = new PdfPTable(2);

            pdfTable.setWidthPercentage(100);

            pdfTable.addCell(new Phrase("Category"));
            pdfTable.addCell(new Phrase("Amount (LKR)"));

            HashMap<String, Double> categoryTotals = new HashMap<>();

            for (Expense expense : Main.expenses) {

                try {

                    YearMonth expenseMonth =
                            YearMonth.from(
                                    java.time.LocalDate.parse(expense.getDate())
                            );

                    if (expenseMonth.equals(selectedMonth)) {

                        String category = expense.getCategory();

                        categoryTotals.put(
                                category,
                                categoryTotals.getOrDefault(category, 0.0)
                                        + expense.getAmount()
                        );
                    }

                } catch (Exception ex) {
                    System.out.println("Invalid expense date.");
                }
            }

            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {

                pdfTable.addCell(entry.getKey());

                pdfTable.addCell(
                        currency.format(entry.getValue())
                );
            }

            document.add(pdfTable);

            document.close();

            JOptionPane.showMessageDialog(
                    this,
                    "Monthly report exported successfully!"
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "PDF Export Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Custom bar chart
    private static class ChartPanel extends JPanel {

        private double income;
        private double expenses;

        public void setValues(double income, double expenses) {

            this.income = income;
            this.expenses = expenses;
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            int width = getWidth();
            int height = getHeight();

            int chartHeight = height - 100;
            int baseY = height - 55;

            double maximum = Math.max(income, expenses);

            if (maximum <= 0) {
                g2.drawString(
                        "No financial data available for this month.",
                        30,
                        height / 2
                );
                return;
            }

            int barWidth = 65;

            int incomeHeight =
                    (int) ((income / maximum) * chartHeight);

            int expenseHeight =
                    (int) ((expenses / maximum) * chartHeight);

            int incomeX = width / 4 - barWidth / 2;
            int expenseX = (width * 3) / 4 - barWidth / 2;

            g2.setColor(new Color(46, 139, 87));

            g2.fillRect(
                    incomeX,
                    baseY - incomeHeight,
                    barWidth,
                    incomeHeight
            );

            g2.setColor(new Color(220, 80, 80));

            g2.fillRect(
                    expenseX,
                    baseY - expenseHeight,
                    barWidth,
                    expenseHeight
            );

            g2.setColor(Color.BLACK);

            g2.drawString("Income", incomeX + 5, baseY + 20);
            g2.drawString("Expenses", expenseX - 5, baseY + 20);

            g2.drawString(
                    String.format("%.2f", income),
                    incomeX - 10,
                    baseY - incomeHeight - 8
            );

            g2.drawString(
                    String.format("%.2f", expenses),
                    expenseX - 10,
                    baseY - expenseHeight - 8
            );

            g2.drawLine(20, baseY, width - 20, baseY);
        }
    }
}

