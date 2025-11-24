package com.one.aim.rq;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRq {
    private String orderId;   // ORD-XXXXXX (Business ID)
}