package com.one.aim.service.impl;

import java.time.LocalDateTime;

import com.one.aim.rq.CancelPaymentRq;
import com.one.aim.rq.CreatePaymentRq;
import com.one.aim.rq.VerifyPaymentRq;
import com.one.aim.rs.CreatePaymentRs;
import com.one.aim.rs.VerifyPaymentRs;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.one.aim.bo.OrderBO;
import com.one.aim.bo.PaymentBO;
import com.one.aim.repo.OrderRepo;
import com.one.aim.repo.PaymentRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.service.PaymentService;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import com.razorpay.RazorpayClient;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepo userRepo;
    private final PaymentRepo paymentRepo;
    private final OrderRepo orderRepo;
    private final UserActivityService userActivityService;

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    // ============================================================
    // 1. CREATE RAZORPAY ORDER
    // ============================================================
    @Override
    public BaseRs createRazorpayOrder(CreatePaymentRq rq) throws Exception {

        // Load order using BUSINESS orderId (ORD-XXXXXX)
        OrderBO order = orderRepo.findByOrderId(rq.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
            return ResponseUtils.failure("COD_NOT_ALLOWED_FOR_ONLINE_PAYMENT");
        }

        Long amount = order.getTotalAmount(); // rupees

        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject options = new JSONObject();
        options.put("amount", amount * 100);   // paise
        options.put("currency", "INR");
        options.put("receipt", order.getOrderId());

        com.razorpay.Order razorpayOrder = razorpay.orders.create(options);
        String razorpayOrderId = razorpayOrder.get("id");

        // Save details in DB
        order.setRazorpayorderid(razorpayOrderId);
        order.setPaymentStatus("CREATED");
        orderRepo.save(order);

        userActivityService.log(
                order.getUser().getId(),
                "PAYMENT_INITIATED",
                "Payment started for order: " + order.getOrderId()
        );

        CreatePaymentRs data = new CreatePaymentRs(
                razorpayOrderId,
                amount * 100,
                "INR",
                razorpayKeyId,
                order.getOrderId()
        );

        return ResponseUtils.success(data);
    }

    // ============================================================
    // 2. VERIFY PAYMENT SIGNATURE
    // ============================================================
    @Override
    public BaseRs verifyRazorpayPayment(VerifyPaymentRq rq) throws Exception {

        OrderBO order = orderRepo.findByOrderId(rq.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String payload = rq.getRazorpayOrderId() + "|" + rq.getRazorpayPaymentId();
        String generatedSignature = hmacSHA256_HEX(payload, razorpayKeySecret);

        log.info("Generated Signature = {}", generatedSignature);
        log.info("Client Signature    = {}", rq.getRazorpaySignature());

        if (!generatedSignature.equals(rq.getRazorpaySignature())) {

            order.setPaymentStatus("FAILED");
            orderRepo.save(order);

            userActivityService.log(
                    order.getUser().getId(),
                    "PAYMENT_FAILED",
                    "Razorpay signature mismatch for order: " + order.getOrderId()
            );

            return ResponseUtils.failure("PAYMENT_VERIFICATION_FAILED");
        }

        // Payment successful
        order.setPaymentStatus("PAID");
        order.setRazorpayPaymentId(rq.getRazorpayPaymentId());
        order.setRazorpaySignature(rq.getRazorpaySignature());
        orderRepo.save(order);

        // Save payment record
        PaymentBO payment = new PaymentBO();
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(order.getPaymentMethod());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setUser(order.getUser());
        payment.setOrder(order);
        payment.setStatus("PAID");
        payment.setRazorpayOrderId(rq.getRazorpayOrderId());
        payment.setRazorpayPaymentId(rq.getRazorpayPaymentId());
        paymentRepo.save(payment);

        userActivityService.log(
                order.getUser().getId(),
                "PAYMENT_SUCCESS",
                "Payment successful for order: " + order.getOrderId()
        );

        VerifyPaymentRs rs = new VerifyPaymentRs(order.getOrderId(), "PAID");
        return ResponseUtils.success(rs);
    }

    // ============================================================
    // Razorpay uses HEX-HMAC (NOT Base64)
    // ============================================================
    private String hmacSHA256_HEX(String data, String key) throws Exception {
        Mac sha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256.init(secretKey);

        byte[] hash = sha256.doFinal(data.getBytes());

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    @Override
    public BaseRs cancelPayment(CancelPaymentRq rq) throws Exception {

        OrderBO order = orderRepo.findByOrderId(rq.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Log user activity
        userActivityService.log(
                order.getUser().getId(),
                "PAYMENT_CANCELLED",
                "User cancelled payment for order: " + order.getOrderId()
        );

        return ResponseUtils.success("Payment cancelled logged successfully");
    }

}
