package com.greenride.greenride.controller;

import com.greenride.greenride.dto.PaymentRequest;
import com.greenride.greenride.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/external/bank")
public class MockPaymentController {

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {

        // 1. Simulate Network Delay
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        System.out.println("🏦 BANK: Processing payment of $" + request.getAmount() + " for user " + request.getUserId());

        // 2. SIMULATED LOGIC:
        // Rule: If the price is exactly $999, we DECLINE it (Simulate insufficient funds).
        if (request.getAmount() == 999.0) {
            System.out.println("🏦 BANK: Payment Declined!");
            return ResponseEntity.ok(new PaymentResponse("FAILED", null));
        }

        System.out.println("🏦 BANK: Payment Approved!");
        return ResponseEntity.ok(new PaymentResponse("SUCCESS", UUID.randomUUID().toString()));
    }
}