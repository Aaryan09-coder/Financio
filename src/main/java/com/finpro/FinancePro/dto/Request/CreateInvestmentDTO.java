package com.finpro.FinancePro.dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateInvestmentDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Investment type is required")
    private String type; // e.g., "Stock", "Bond", "Mutual Fund"

    private String description;

    @NotNull(message = "Stock symbol is required")
    private String symbol;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double quantity;

    @NotNull(message = "Purchase price is required")
    @Positive(message = "Purchase price must be positive")
    private Double purchasePrice;

    public @NotNull(message = "User ID is required") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID is required") Long userId) {
        this.userId = userId;
    }

    public @NotNull(message = "Investment type is required") String getType() {
        return type;
    }

    public void setType(@NotNull(message = "Investment type is required") String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public @NotNull(message = "Stock symbol is required") String getSymbol() {
        return symbol;
    }

    public void setSymbol(@NotNull(message = "Stock symbol is required") String symbol) {
        this.symbol = symbol;
    }

    public @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") Double getQuantity() {
        return quantity;
    }

    public void setQuantity(@NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") Double quantity) {
        this.quantity = quantity;
    }

    public @NotNull(message = "Purchase price is required") @Positive(message = "Purchase price must be positive") Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(@NotNull(message = "Purchase price is required") @Positive(message = "Purchase price must be positive") Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
}
