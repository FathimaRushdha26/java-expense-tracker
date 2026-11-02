
import java.time.LocalDate;

public class FinancialGoal {

    private int id;
    private String goalName;
    private double targetAmount;
    private double currentAmount;
    private LocalDate targetDate;
    private boolean completed;

    public FinancialGoal(int id, String goalName,
                         double targetAmount, double currentAmount,
                         LocalDate targetDate, boolean completed) {

        this.id = id;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public String getGoalName() {
        return goalName;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public double getProgress() {
        if (targetAmount <= 0) {
            return 0;
        }

        return Math.min((currentAmount / targetAmount) * 100, 100);
    }

    public double getRemainingAmount() {
        return Math.max(targetAmount - currentAmount, 0);
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
        this.completed = currentAmount >= targetAmount;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

