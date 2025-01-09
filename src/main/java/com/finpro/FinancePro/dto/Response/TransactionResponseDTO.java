package com.finpro.FinancePro.dto.Response;

public class TransactionResponseDTO {
    private Long id;
    private String category;
    private double amount;
    private String type; // INCOME or EXPENSE
    private String description;
    private Long userId;
    //private String createdAt; // Optional: Format to a readable date-time
    //private String updatedAt; // Optional: Format to a readable date-time


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
