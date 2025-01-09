package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateGoalDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    private double targetAmount;

    public @NotNull(message = "User ID is required") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID is required") Long userId) {
        this.userId = userId;
    }

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(@NotNull(message = "Target amount is required") @Positive(message = "Target amount must be positive") double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public @NotNull(message = "Name is required") String getName() {
        return name;
    }

    public void setName(@NotNull(message = "Name is required") String name) {
        this.name = name;
    }
}
