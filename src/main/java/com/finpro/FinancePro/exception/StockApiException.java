package com.finpro.FinancePro.exception;

public class StockApiException extends RuntimeException {
    public StockApiException(String message) {
        super(message);
    }
}
