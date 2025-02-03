package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.NotNull;

public class UpdateTransactionDTO {

    @NotNull(message = "Transaction ID is required")
    private Long id;

    @NotNull(message = "Category is required")
    private String category;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Type is required")
    private String type;

    private String description;

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
