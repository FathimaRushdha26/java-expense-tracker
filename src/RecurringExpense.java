
import java.time.LocalDate;

public class RecurringExpense {

    private int id;
    private String description;
    private double amount;
    private String category;
    private String frequency;
    private LocalDate nextPaymentDate;
    private boolean active;
    private LocalDate lastGeneratedDate;

    public RecurringExpense(
            int id,
            String description,
            double amount,
            String category,
            String frequency,
            LocalDate nextPaymentDate,
            boolean active,
            LocalDate lastGeneratedDate
    ) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.frequency = frequency;
        this.nextPaymentDate = nextPaymentDate;
        this.active = active;
        this.lastGeneratedDate = lastGeneratedDate;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getFrequency() {
        return frequency;
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getLastGeneratedDate() {
        return lastGeneratedDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setNextPaymentDate(LocalDate nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setLastGeneratedDate(LocalDate lastGeneratedDate) {
        this.lastGeneratedDate = lastGeneratedDate;
    }
}

