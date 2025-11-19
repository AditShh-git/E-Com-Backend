package com.one.aim.rq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRq {
    private String orderId;              // ORD-XXXXXX
    private String razorpayPaymentId;    // pay_XXXX
    private String razorpayOrderId;      // order_XXXX
    private String razorpaySignature;    // HMAC signature
}
