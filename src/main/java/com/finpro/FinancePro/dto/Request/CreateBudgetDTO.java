package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateBudgetDTO {

    @Min(value = 0, message = "Total amount must be greater than or equal to 0")
    private double totalAmount;

    @NotBlank(message = "Period cannot be blank")
    private String period;

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
