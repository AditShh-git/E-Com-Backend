package com.one.aim.service;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.UserBO;
import com.one.service.impl.UserDetailsImpl;
import com.one.vm.core.BaseRs;

public interface EmailService {

    // ===========================================================
    // Verification Email
    // ===========================================================
    /**
     * Sends an email verification link to the given address.
     *
     * @param toEmail   Recipient's email address
     * @param fullName  Recipient's full name
     * @param token     Verification token
     */
    void sendVerificationEmail(String toEmail, String fullName, String token);

    // ===========================================================
    // Password Reset Email
    // ===========================================================
    /**
     * Sends a password reset email to the given address.
     *
     * @param toEmail   Recipient's email address
     * @param token     Password reset token
     */
    void sendResetPasswordEmail(String toEmail, String token);

    // ===========================================================
    // Welcome Email
    // ===========================================================
    /**
     * Sends a welcome email to the given address.
     *
     * @param toEmail   Recipient's email address
     * @param fullName  Recipient's full name
     */
    void sendWelcomeEmail(String toEmail, String fullName);

    // ===========================================================
    // Seller Under Review
    // ===========================================================
    /**
     * Sends an email informing the seller that their email is verified
     * and their account is under review.
     *
     * @param toEmail   Seller's email address
     * @param fullName  Seller's full name
     */
    void sendSellerUnderReviewEmail(String toEmail, String fullName);

    // ===========================================================
    // Seller Approval Email
    // ===========================================================
    /**
     * Sends an email informing the seller that admin approved their account.
     *
     * @param toEmail   Seller's email address
     * @param fullName  Seller's full name
     */
    void sendSellerApprovalEmail(String toEmail, String fullName);

    // ===========================================================
    // Verification Logic
    // ===========================================================
    /**
     * Verifies the email using token + email (User + Seller + Admin).
     */
    BaseRs verifyEmail(String token, String email);

    /**
     * Resends verification email to User/Seller/Admin.
     */
    BaseRs resendVerificationEmail(String email);

    /**
     * Ensure email is verified before login
     * Throws error if not verified.
     */
    void checkEmailVerified(UserDetailsImpl userDetails);

    // ===========================================================
    // Email Change Logic
    // ===========================================================
    /**
     * Initiates email change for USER.
     */
    void initiateUserEmailChange(UserBO user, String newEmail);

    /**
     * Initiates email change for SELLER.
     */
    void initiateSellerEmailChange(SellerBO seller, String newEmail);

    /**
     * Initiates email change for ADMIN.
     */
    void initiateAdminEmailChange(AdminBO admin, String newEmail);
}
