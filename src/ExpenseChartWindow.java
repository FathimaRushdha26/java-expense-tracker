
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

public class ExpenseChartWindow extends JFrame {

    public ExpenseChartWindow() {

        setTitle("Expense Tracker - Category Chart");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new ChartPanel());

        setVisible(true);
    }

    class ChartPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString("CATEGORY-WISE EXPENSE CHART", 200, 40);

            TreeMap<String, Double> totals =
                    new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

            for (Expense expense : Main.expenses) {

                String category = expense.getCategory();

                totals.put(
                        category,
                        totals.getOrDefault(category, 0.0)
                                + expense.getAmount()
                );
            }

            if (totals.isEmpty()) {

                g2.drawString("No expenses available.", 300, 200);
                return;
            }

            double max = 0;

            for (double value : totals.values()) {
                max = Math.max(max, value);
            }

            int startX = 100;
            int startY = 100;
            int chartHeight = 300;

            int availableWidth = 650;
            int count = totals.size();

            int gap = Math.max(10, Math.min(30, 500 / count));
            int barWidth = Math.max(
                    15,
                    Math.min(65, (availableWidth - gap * count) / count)
            );

            g2.setColor(Color.BLACK);

            g2.drawLine(
                    startX, startY,
                    startX, startY + chartHeight
            );

            g2.drawLine(
                    startX, startY + chartHeight,
                    780, startY + chartHeight
            );

            int x = startX + 25;

            for (Map.Entry<String, Double> entry : totals.entrySet()) {

                double amount = entry.getValue();

                int barHeight = (int) (
                        amount / max * (chartHeight - 30)
                );

                int y = startY + chartHeight - barHeight;

                g2.setColor(new Color(55, 120, 210));

                g2.fillRoundRect(
                        x, y, barWidth, barHeight, 10, 10
                );

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 12));

                String value = String.format("%.2f", amount);

                g2.drawString(value, x, y - 8);

                String category = entry.getKey();

                if (category.length() > 10) {
                    category = category.substring(0, 10);
                }

                g2.drawString(
                        category,
                        x - 5,
                        startY + chartHeight + 20
                );

                x += barWidth + gap;
            }

            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            g2.drawString("Amount (Rs.)", 10, 90);
        }
    }
}
