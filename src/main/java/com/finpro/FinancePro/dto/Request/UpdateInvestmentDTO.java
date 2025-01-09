package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.Positive;

public class UpdateInvestmentDTO {

    private String type;

    private String description;

    @Positive(message = "Quantity must be positive")
    private Double quantity;

    @Positive(message = "Purchase price must be positive")
    private Double purchasePrice;

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

    public @Positive(message = "Quantity must be positive") Double getQuantity() {
        return quantity;
    }

    public void setQuantity(@Positive(message = "Quantity must be positive") Double quantity) {
        this.quantity = quantity;
    }

    public @Positive(message = "Purchase price must be positive") Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(@Positive(message = "Purchase price must be positive") Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
}
