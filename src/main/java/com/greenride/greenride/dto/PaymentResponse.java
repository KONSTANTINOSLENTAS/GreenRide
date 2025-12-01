package com.greenride.greenride.dto;

public class PaymentResponse {
    private String status; // "SUCCESS" or "FAILED"
    private String transactionId;

    public PaymentResponse() {} // Empty constructor needed for JSON parsing

    public PaymentResponse(String status, String transactionId) {
        this.status = status;
        this.transactionId = transactionId;
    }

    public String getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
}