package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateGoalDTO {

    @NotNull(message = "Goal ID is required")
    private Long id;

    @Min(value = 0, message = "Target amount must be zero or positive")
    private double targetAmount;

    public @NotNull(message = "Goal ID is required") Long getId() {
        return id;
    }

    public void setId(@NotNull(message = "Goal ID is required") Long id) {
        this.id = id;
    }

    @Min(value = 0, message = "Target amount must be zero or positive")
    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(@Min(value = 0, message = "Target amount must be zero or positive") double targetAmount) {
        this.targetAmount = targetAmount;
    }
}
