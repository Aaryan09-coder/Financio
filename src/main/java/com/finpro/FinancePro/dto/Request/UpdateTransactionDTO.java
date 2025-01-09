package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.NotNull;

public class UpdateTransactionDTO {

    @NotNull(message = "Transaction ID is required")
    private Long id;

    private String category; // Optional
    private Double amount;   // Optional
    private String type;     // Optional
    private String description; // Optional

    public @NotNull(message = "Transaction ID is required") Long getId() {
        return id;
    }

    public void setId(@NotNull(message = "Transaction ID is required") Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
