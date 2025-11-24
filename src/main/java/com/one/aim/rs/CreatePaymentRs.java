package com.one.aim.rs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreatePaymentRs {

    private String razorpayOrderId;   // from Razorpay
    private Long amount;              // in paise
    private String currency;          // INR
    private String key;               // Razorpay key_id

    private String orderId;           // ORD-XXXXXX
}

