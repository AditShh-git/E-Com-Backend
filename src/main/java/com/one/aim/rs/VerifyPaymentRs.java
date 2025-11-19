package com.one.aim.rs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VerifyPaymentRs {
    private String orderId;     // ORD-XXXXXX
    private String status;      // PAID or FAILED
}
