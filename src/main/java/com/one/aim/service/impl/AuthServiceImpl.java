package com.one.aim.service.impl;

import com.one.aim.bo.UserBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.repo.UserRepo;
import com.one.aim.rs.ResetPasswordRs;
import com.one.aim.rs.data.LoginDataRs;
import com.one.aim.service.AuthService;
import com.one.aim.service.EmailService;
import com.one.security.jwt.JwtUtils;
import com.one.service.impl.UserDetailsImpl;
import com.one.utils.AuthUtils;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JavaMailSender javaMailSender;
    private final EmailService emailService;

    // ============================================
    //  SIGN IN
    // ============================================
    @Override
    public BaseRs signIn(Authentication authentication) throws Exception {
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        String message = MessageCodes.MC_LOGIN_SUCCESSFUL;
        return ResponseUtils.success(new LoginDataRs(
                message,
                accessToken,
                refreshToken,
                userDetails.getId(),
                userDetails.getEmail(),     //  email as main identity
                userDetails.getFullName(),
                userDetails.getEmail()
        ));
    }

    // ============================================
    //  FORGOT PASSWORD
    // ============================================
    @Transactional
    @Override
    public BaseRs forgotPassword(String email) {
        Optional<UserBO> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND, "Email not registered");
        }

        UserBO user = userOpt.get();

        //  Generate token & expiry
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepo.save(user);

        //  Send actual email
        String resetLink = "https://yourfrontend.com/reset-password?token=" + resetToken;
        String subject = "Password Reset Request";
        String body = "Hello " + user.getFullName() + ",\n\n"
                + "Click the link below to reset your password:\n"
                + resetLink + "\n\n"
                + "This link will expire in 30 minutes.\n\n"
                + "Best regards,\nYour App Team";

        try {
            //  Send email (assuming you have JavaMailSender configured)
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            log.info(" Password reset email sent to {}", email);
        } catch (Exception e) {
            log.error(" Failed to send reset email: {}", e.getMessage());
            return ResponseUtils.failure("Failed to send email. Please try again later.");
        }

        //  Return success + token for testing
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset link sent to email");
        response.put("resetToken", resetToken); // only for testing — remove in production

        return ResponseUtils.success(
                new BaseDataRs("Password reset link sent to email", new ResetPasswordRs(
                        "Password reset link sent to email",
                        resetToken
                ))
        );

    }

    // ============================================
//  RESET PASSWORD (All roles supported)
// ============================================
    @Transactional
    @Override
    public BaseRs resetPassword(String token, String newPassword) {
        //  Lookup user by reset token
        Optional<UserBO> optUser = userRepo.findByResetToken(token);
        if (optUser.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_TOKEN, "Invalid or expired reset token");
        }

        UserBO user = optUser.get();

        //  Check if token expired
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseUtils.failure(ErrorCodes.EC_TOKEN_EXPIRED, "Reset token expired");
        }

        //  Encode and update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepo.save(user);

        log.info(" Password reset for {} (Role: {})", user.getEmail(), user.getRole());

        //  Return success response
        return ResponseUtils.success(
                new BaseDataRs("Password successfully reset", Map.of("role", user.getRole()))
        );
    }

    // ============================================
    //  LOGOUT
    // ============================================
    @Transactional
    @Override
    public BaseRs logout() throws Exception {
        Long id = AuthUtils.findLoggedInUser().getDocId();
        userRepo.findById(id).ifPresent(user -> {
            user.setLogin(false);
            userRepo.save(user);
        });
        SecurityContextHolder.clearContext();
        return ResponseUtils.success(MessageCodes.MC_LOGOUT_SUCCESSFUL);
    }

    // ===========================================================
    // VERIFY EMAIL
    // ===========================================================
    @Override
    public BaseRs verifyEmail(String token) {
        Optional<UserBO> optUser = userRepo.findByVerificationToken(token);
        if (optUser.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_TOKEN, "Invalid or expired token");
        }

        UserBO user = optUser.get();

        // Check expiry
        if (user.getVerificationTokenExpiry() == null || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseUtils.failure("TOKEN_EXPIRED", "Verification link has expired.");
        }

        // If it was a pending email update, apply it
        if (user.getPendingEmail() != null && !user.getPendingEmail().isBlank()
                && !user.getPendingEmail().equalsIgnoreCase(user.getEmail())) {

            log.info("User {} verified new email: {}", user.getId(), user.getPendingEmail());
            user.setEmail(user.getPendingEmail().toLowerCase());
            user.setPendingEmail(null);
        }

        // Mark as verified and activate account
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        // Optionally mark active on email verification
        if (!user.isActive()) {
            user.setActive(true);
        }

        userRepo.save(user);
        return ResponseUtils.success("Email verified successfully.");
    }

    // ===========================================================
    // RESEND VERIFICATION EMAIL
    // ===========================================================
    @Override
    public BaseRs resendVerificationEmail(String email) {
        log.debug("Resending verification email to: {}", email);

        Optional<UserBO> optUser = userRepo.findByEmail(email.toLowerCase());
        if (optUser.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
        }

        UserBO user = optUser.get();
        if (user.getEmailVerified()) {
            return ResponseUtils.failure(
                    ErrorCodes.EC_EMAIL_ALREADY_VERIFIED,
                    "Email already verified."
            );
        }

        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepo.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), newToken);
        return ResponseUtils.success("Verification email resent successfully!");
    }
}
