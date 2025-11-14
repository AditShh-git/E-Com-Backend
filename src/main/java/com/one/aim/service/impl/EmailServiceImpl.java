package com.one.aim.service.impl;

import com.one.aim.service.EmailService;
import com.one.exception.EmailSendFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@oneaim.com}")
    private String fromEmail;

    // ===========================================================
    //  Verification Email
    // ===========================================================
    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        try {
            String subject = "Verify Your Email - OneAim Platform";
            String verificationLink = frontendUrl + "/verify-email?token=" + token;
            String htmlContent = buildVerificationEmailTemplate(fullName, verificationLink);

            sendHtmlEmail(toEmail, subject, htmlContent);
            log.info(" Verification email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error(" Failed to send verification email to: {}", toEmail, e);
            throw new EmailSendFailedException("Unable to send verification email. Please try again later.");
        }
    }

    // ===========================================================
    //  Reset Password Email
    // ===========================================================
    @Async
    public void sendResetPasswordEmail(String toEmail, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String subject = "Reset your password";
            String htmlContent = buildResetPasswordEmailTemplate(resetUrl);

            sendHtmlEmail(toEmail, subject, htmlContent);
            log.info(" Password reset email sent to {}", toEmail);

        } catch (Exception e) {
            log.error(" Failed to send reset password email: {}", e.getMessage());
            throw new EmailSendFailedException("Unable to send password reset email. Please try again later.");
        }
    }

    // ===========================================================
    //  Welcome Email
    // ===========================================================
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            String subject = "Welcome to OneAim! 🎉";
            String htmlContent = buildWelcomeEmailTemplate(fullName);

            sendHtmlEmail(toEmail, subject, htmlContent);
            log.info(" Welcome email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error(" Failed to send welcome email to: {}", toEmail, e);
            throw new EmailSendFailedException("Unable to send welcome email. Please try again later.");
        }
    }

    // ===========================================================
    //  Reusable HTML Mail Sender
    // ===========================================================
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    // ===========================================================
    //  Templates
    // ===========================================================

    //  Verification Template
    private String buildVerificationEmailTemplate(String fullName, String verificationLink) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2>Hi %s,</h2>
                    <p>Thank you for signing up with <strong>OneAim Platform</strong>! Please verify your email by clicking the button below:</p>
                    <p style="text-align: center;">
                        <a href="%s" style="background-color: #007BFF; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                            Verify Email
                        </a>
                    </p>
                    <p>This link will expire in 15 minutes.</p>
                    <p>Thanks,<br>Team OneAim</p>
                </body>
                </html>
                """.formatted(fullName, verificationLink);
    }

    //  Reset Password Template
    private String buildResetPasswordEmailTemplate(String resetUrl) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2>Password Reset Request</h2>
                    <p>We received a request to reset your password for your <strong>OneAim</strong> account.</p>
                    <p style="text-align: center;">
                        <a href="%s" style="background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                            Reset Password
                        </a>
                    </p>
                    <p>This link will expire in 15 minutes. If you didn’t request this, please ignore this email.</p>
                    <p>Thanks,<br>Team OneAim</p>
                </body>
                </html>
                """.formatted(resetUrl);
    }

    //  Welcome Template
    private String buildWelcomeEmailTemplate(String fullName) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2>Welcome, %s </h2>
                    <p>We’re thrilled to have you on board! Explore the OneAim platform and make the most out of your journey.</p>
                    <p>Start your journey here:</p>
                    <p style="text-align: center;">
                        <a href="%s" style="background-color: #007BFF; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                            Go to OneAim
                        </a>
                    </p>
                    <p>Have questions? Just reply to this email — we’re here to help.</p>
                    <p>Cheers,<br>Team OneAim </p>
                </body>
                </html>
                """.formatted(fullName, frontendUrl);
    }
}

