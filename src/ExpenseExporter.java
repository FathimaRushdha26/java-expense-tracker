
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ExpenseExporter {

    public static boolean exportExpenses(
            ArrayList<Expense> expenses,
            String filePath) {

        try (FileWriter writer = new FileWriter(filePath)) {

            // CSV Header
            writer.append("ID,Description,Amount,Category,Date\n");

            // Expense Data
            for (Expense expense : expenses) {

                writer.append(String.valueOf(expense.getId()))
                        .append(",");

                writer.append("\"")
                        .append(expense.getDescription().replace("\"", "\"\""))
                        .append("\",");

                writer.append(String.valueOf(expense.getAmount()))
                        .append(",");

                writer.append("\"")
                        .append(expense.getCategory().replace("\"", "\"\""))
                        .append("\",");

                writer.append(expense.getDate())
                        .append("\n");
            }

            writer.flush();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

