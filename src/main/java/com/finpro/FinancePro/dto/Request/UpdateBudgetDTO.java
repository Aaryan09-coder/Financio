package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateBudgetDTO {

    @NotNull(message = "Budget ID cannot be null")
    private Long id; // The ID of the budget to update

    @Min(value = 0, message = "Total amount must be greater than or equal to 0")
    private double totalAmount;

    public @NotNull(message = "Budget ID cannot be null") Long getId() {
        return id;
    }

    public void setId(@NotNull(message = "Budget ID cannot be null") Long id) {
        this.id = id;
    }

    @Min(value = 0, message = "Total amount must be greater than or equal to 0")
    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(@Min(value = 0, message = "Total amount must be greater than or equal to 0") double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
