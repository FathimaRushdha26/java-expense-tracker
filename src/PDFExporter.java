
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.TreeMap;

public class PDFExporter {

    public static boolean exportReport(
            String filePath,
            String username
    ) {

        Document document = new Document(PageSize.A4);

        try {

            PdfWriter.getInstance(
                    document,
                    new FileOutputStream(filePath)
            );

            document.open();

            // Fonts
            Font titleFont = new Font(
                    Font.FontFamily.HELVETICA,
                    20,
                    Font.BOLD
            );

            Font headingFont = new Font(
                    Font.FontFamily.HELVETICA,
                    14,
                    Font.BOLD
            );

            Font normalFont = new Font(
                    Font.FontFamily.HELVETICA,
                    10
            );

            Font headerFont = new Font(
                    Font.FontFamily.HELVETICA,
                    10,
                    Font.BOLD
            );

            // Report Title
            Paragraph title = new Paragraph(
                    "PERSONAL EXPENSE REPORT",
                    titleFont
            );

            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // User Information
            document.add(new Paragraph(
                    "Username: " + username,
                    normalFont
            ));

            document.add(new Paragraph(
                    "Report Date: " + LocalDate.now(),
                    normalFont
            ));

            document.add(new Paragraph(" "));

            // 1. Expense Overview
            document.add(new Paragraph(
                    "1. Expense Overview",
                    headingFont
            ));

            double total = 0;

            for (Expense expense : Main.expenses) {
                total += expense.getAmount();
            }

            document.add(new Paragraph(
                    "Total Expenses: Rs. "
                            + String.format("%.2f", total),
                    normalFont
            ));

            YearMonth currentMonth = YearMonth.now();

            double monthlyTotal =
                    Main.getMonthlyTotal(currentMonth);

            document.add(new Paragraph(
                    "Current Month Expenses: Rs. "
                            + String.format("%.2f", monthlyTotal),
                    normalFont
            ));

            double budget = Main.budgets.getOrDefault(
                    currentMonth.toString(),
                    0.0
            );

            document.add(new Paragraph(
                    "Monthly Budget: Rs. "
                            + String.format("%.2f", budget),
                    normalFont
            ));

            document.add(new Paragraph(" "));

            // 2. Category-wise Summary
            document.add(new Paragraph(
                    "2. Category-wise Summary",
                    headingFont
            ));

            TreeMap<String, Double> categoryTotals =
                    new TreeMap<>(
                            String.CASE_INSENSITIVE_ORDER
                    );

            for (Expense expense : Main.expenses) {

                String category = expense.getCategory();

                categoryTotals.put(
                        category,
                        categoryTotals.getOrDefault(category, 0.0)
                                + expense.getAmount()
                );
            }

            PdfPTable categoryTable = new PdfPTable(2);
            categoryTable.setWidthPercentage(100);

            PdfPCell categoryHeader = new PdfPCell(
                    new Paragraph("Category", headerFont)
            );

            PdfPCell amountHeader = new PdfPCell(
                    new Paragraph("Total Amount", headerFont)
            );

            categoryTable.addCell(categoryHeader);
            categoryTable.addCell(amountHeader);

            for (String category : categoryTotals.keySet()) {

                categoryTable.addCell(category);

                categoryTable.addCell(
                        "Rs. " + String.format(
                                "%.2f",
                                categoryTotals.get(category)
                        )
                );
            }

            document.add(categoryTable);

            document.add(new Paragraph(" "));

            // 3. Expense Details
            document.add(new Paragraph(
                    "3. Expense Details",
                    headingFont
            ));

            PdfPTable expenseTable = new PdfPTable(5);

            expenseTable.setWidthPercentage(100);

            expenseTable.setWidths(
                    new float[]{
                            0.7f, 2f, 1.2f, 1.5f, 1.5f
                    }
            );

            expenseTable.setHeaderRows(1);

            expenseTable.addCell(
                    new PdfPCell(new Paragraph("ID", headerFont))
            );

            expenseTable.addCell(
                    new PdfPCell(new Paragraph("Description", headerFont))
            );

            expenseTable.addCell(
                    new PdfPCell(new Paragraph("Amount", headerFont))
            );

            expenseTable.addCell(
                    new PdfPCell(new Paragraph("Category", headerFont))
            );

            expenseTable.addCell(
                    new PdfPCell(new Paragraph("Date", headerFont))
            );

            for (Expense expense : Main.expenses) {

                expenseTable.addCell(
                        String.valueOf(expense.getId())
                );

                expenseTable.addCell(
                        expense.getDescription()
                );

                expenseTable.addCell(
                        "Rs. " + String.format(
                                "%.2f",
                                expense.getAmount()
                        )
                );

                expenseTable.addCell(
                        expense.getCategory()
                );

                expenseTable.addCell(
                        expense.getDate()
                );
            }

            document.add(expenseTable);

            document.add(new Paragraph(" "));

            // Footer
            Paragraph footer = new Paragraph(
                    "Generated by Personal Expense Tracker",
                    normalFont
            );

            footer.setAlignment(Element.ALIGN_CENTER);

            document.add(footer);

            document.close();

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            if (document.isOpen()) {
                document.close();
            }

            return false;
        }
    }
}
