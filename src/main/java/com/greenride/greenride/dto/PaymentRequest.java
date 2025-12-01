package com.greenride.greenride.dto;

public class PaymentRequest {
    private String userId;
    private Double amount;

    public PaymentRequest(String userId, Double amount) {
        this.userId = userId;
        this.amount = amount;
    }

    // Getters
    public String getUserId() { return userId; }
    public Double getAmount() { return amount; }
}