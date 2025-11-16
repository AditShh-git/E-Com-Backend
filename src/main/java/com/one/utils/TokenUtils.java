package com.one.utils;

import java.time.LocalDateTime;
import java.util.UUID;

public class TokenUtils {

    // Generate strong verification / reset tokens
    public static String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
    }

    // Expiry → Default 15 minutes
    public static LocalDateTime generateExpiry() {
        return LocalDateTime.now().plusMinutes(15);
    }

    // Expiry with custom minutes
    public static LocalDateTime generateExpiry(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }
}
