package com.one.aim.controller;

import com.one.aim.rq.CancelPaymentRq;
import com.one.aim.rq.CreatePaymentRq;
import com.one.aim.rq.VerifyPaymentRq;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.bo.OrderBO;
import com.one.aim.repo.OrderRepo;
import com.one.aim.rq.PaymentRq;
import com.one.aim.service.PaymentService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ============================================================
    // 1. Create Razorpay Order
    // ============================================================
    @PostMapping("/create")
    public ResponseEntity<?> createPaymentOrder(@RequestBody CreatePaymentRq rq) throws Exception {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(rq));
    }

    // ============================================================
    // 2. Verify Razorpay Payment
    // ============================================================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody VerifyPaymentRq rq) throws Exception {
        return ResponseEntity.ok(paymentService.verifyRazorpayPayment(rq));
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(@RequestBody CancelPaymentRq rq) throws Exception {
        return ResponseEntity.ok(paymentService.cancelPayment(rq));
    }


}
