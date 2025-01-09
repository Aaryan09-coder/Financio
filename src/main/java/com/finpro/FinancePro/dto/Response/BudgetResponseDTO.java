package com.finpro.FinancePro.dto.Response;

public class BudgetResponseDTO {

    private Long id;
    private double totalAmount;
    private double spentAmount;
    private double remainingBudget;
    private String period;


    // Getters and setters for all fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(double remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
