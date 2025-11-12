package com.one.aim.service;

public interface EmailService {

    /**
     * Sends an email verification link to the given address.
     *
     * @param toEmail   Recipient's email address
     * @param fullName  Recipient's full name
     * @param token     Verification token
     */
    void sendVerificationEmail(String toEmail, String fullName, String token);

    /**
     * Sends a password reset email to the given address.
     *
     * @param toEmail   Recipient's email address
     * @param token     Password reset token
     */
    void sendResetPasswordEmail(String toEmail, String token);

    /**
     * Sends a welcome email to the given address.
     *
     * @param toEmail   Recipient's email address
     * @param fullName  Recipient's full name
     */
    void sendWelcomeEmail(String toEmail, String fullName);
}
