package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateBudgetDTO {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Min(value = 0, message = "Total amount must be greater than or equal to 0")
    private double totalAmount;

    @NotBlank(message = "Period cannot be blank")
    private String period;

    public @NotNull(message = "User ID cannot be null") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID cannot be null") Long userId) {
        this.userId = userId;
    }

    @Min(value = 0, message = "Total amount must be greater than or equal to 0")
    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(@Min(value = 0, message = "Total amount must be greater than or equal to 0") double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public @NotBlank(message = "Period cannot be blank") String getPeriod() {
        return period;
    }

    public void setPeriod(@NotBlank(message = "Period cannot be blank") String period) {
        this.period = period;
    }
}
