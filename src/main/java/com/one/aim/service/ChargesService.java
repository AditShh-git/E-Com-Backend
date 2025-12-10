package com.one.aim.service;

import org.springframework.http.ResponseEntity;

public interface ChargesService {
    ResponseEntity<?> calculate(Double subtotal, String shippingMethod, String state);
}

