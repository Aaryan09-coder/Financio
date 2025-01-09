package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.NotNull;

public class CreateTransactionDTO {

    @NotNull(message = "Category is required")
    private String category;

    @NotNull(message = "Amount is required")
    private double amount;

    @NotNull(message = "Type is required") // INCOME or EXPENSE
    private String type;

    private String description;

    @NotNull(message = "User ID is required")
    private Long userId;

    public @NotNull(message = "Category is required") String getCategory() {
        return category;
    }

    public void setCategory(@NotNull(message = "Category is required") String category) {
        this.category = category;
    }

    @NotNull(message = "Amount is required")
    public double getAmount() {
        return amount;
    }

    public void setAmount(@NotNull(message = "Amount is required") double amount) {
        this.amount = amount;
    }

    public @NotNull(message = "Type is required") String getType() {
        return type;
    }

    public void setType(@NotNull(message = "Type is required") String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public @NotNull(message = "User ID is required") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID is required") Long userId) {
        this.userId = userId;
    }
}
