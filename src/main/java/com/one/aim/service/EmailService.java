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

    /**
     * Sends an email informing the seller that
     * their email is verified and their account is under review.
     *
     * @param toEmail   Seller's email address
     * @param fullName  Seller's full name
     */
    void sendSellerUnderReviewEmail(String toEmail, String fullName);

    /**
     * Sends an email informing the seller that
     * admin has approved their account.
     *
     * @param toEmail   Seller's email address
     * @param fullName  Seller's full name
     */
    void sendSellerApprovalEmail(String toEmail, String fullName);
}
