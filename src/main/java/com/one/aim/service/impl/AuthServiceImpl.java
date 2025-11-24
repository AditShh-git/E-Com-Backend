package com.one.aim.service.impl;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.UserBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.rs.ResetPasswordRs;
import com.one.aim.rs.data.LoginDataRs;
import com.one.aim.service.AuthService;
import com.one.aim.service.EmailService;
import com.one.security.LoggedUserContext;
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
    private final SellerRepo sellerRepo;
    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JavaMailSender javaMailSender;
    private final EmailService emailService;

    // ============================================================
    // SIGN IN (User + Seller + Admin)
    // ============================================================
    @Override
    public BaseRs signIn(Authentication authentication) throws Exception {

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        String role = user.getRole();

        // -------------------------------------------------------
        // BLOCK 1: Email verification check (all roles)
        // -------------------------------------------------------
        if (!user.isEmailVerified()) {
            return ResponseUtils.failure(
                    ErrorCodes.EC_ACCOUNT_NOT_VERIFIED,
                    "Please verify your email before logging in."
            );
        }

        // -------------------------------------------------------
        // BLOCK 2: Seller-specific checks
        // -------------------------------------------------------
        if ("SELLER".equals(role)) {

            SellerBO seller = sellerRepo.findById(user.getId()).orElse(null);

            if (seller == null) {
                return ResponseUtils.failure("SELLER_NOT_FOUND");
            }

            // Admin approval check
            if (!seller.isVerified()) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_ACCOUNT_NOT_VERIFIED,
                        "Your seller account is pending admin approval."
                );
            }

            // Locked account check
            if (seller.isLocked()) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_ACCOUNT_LOCKED,
                        "Your seller account has been locked by admin."
                );
            }
        }

        // -------------------------------------------------------
        // BLOCK 3: Admin-specific check (optional)
        // -------------------------------------------------------
        if ("ADMIN".equals(role)) {

            AdminBO admin = adminRepo.findById(user.getId()).orElse(null);

            if (admin == null) {
                return ResponseUtils.failure("ADMIN_NOT_FOUND");
            }

            if (!admin.isEmailVerified()) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_ACCOUNT_NOT_VERIFIED,
                        "Please verify your admin email before logging in."
                );
            }
        }

        // -------------------------------------------------------
        // IF ALL CHECKS PASS → Generate JWT Tokens
        // -------------------------------------------------------
        String accessToken = jwtUtils.generateAccessToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        return ResponseUtils.success(
                new LoginDataRs(
                        MessageCodes.MC_LOGIN_SUCCESSFUL,
                        accessToken,
                        refreshToken,
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole()
                )
        );
    }


    // ============================================================
    // FORGOT PASSWORD (User + Seller + Admin)
    // ============================================================
    @Transactional
    @Override
    public BaseRs forgotPassword(String email) {

        email = email.trim().toLowerCase();

        Optional<UserBO> userOpt = userRepo.findByEmail(email);
        Optional<SellerBO> sellerOpt = Optional.empty();
        Optional<AdminBO> adminOpt = Optional.empty();

        if (userOpt.isEmpty()) {
            sellerOpt = sellerRepo.findByEmailIgnoreCase(email);
        }
        if (userOpt.isEmpty() && sellerOpt.isEmpty()) {
            adminOpt = adminRepo.findByEmailIgnoreCase(email);
        }

        if (userOpt.isEmpty() && sellerOpt.isEmpty() && adminOpt.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND, "Email not registered");
        }

        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);

        if (userOpt.isPresent()) {
            UserBO user = userOpt.get();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(expiry);
            userRepo.save(user);
        } else if (sellerOpt.isPresent()) {
            SellerBO seller = sellerOpt.get();
            seller.setResetToken(resetToken);
            seller.setResetTokenExpiry(expiry);
            sellerRepo.save(seller);
        } else {
            AdminBO admin = adminOpt.get();
            admin.setResetToken(resetToken);
            admin.setResetTokenExpiry(expiry);
            adminRepo.save(admin);
        }

        String resetLink = "https://yourfrontend.com/reset-password?token=" + resetToken;

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Password Reset Request");
            msg.setText("Reset your password using this link:\n" + resetLink);
            javaMailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
            return ResponseUtils.failure(ErrorCodes.EC_EMAIL_SEND_FAILED, "Failed to send reset email.");
        }

        return ResponseUtils.success(
                new ResetPasswordRs("Password reset link sent", resetToken)
        );
    }

    // ============================================================
    // RESET PASSWORD (User + Seller + Admin)
    // ============================================================
    @Transactional
    @Override
    public BaseRs resetPassword(String token, String newPassword) {

        Optional<UserBO> userOpt = userRepo.findByResetToken(token);
        Optional<SellerBO> sellerOpt = Optional.empty();
        Optional<AdminBO> adminOpt = Optional.empty();

        if (userOpt.isEmpty()) {
            sellerOpt = sellerRepo.findByResetToken(token);
        }
        if (userOpt.isEmpty() && sellerOpt.isEmpty()) {
            adminOpt = adminRepo.findByResetToken(token);
        }

        if (userOpt.isEmpty() && sellerOpt.isEmpty() && adminOpt.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_TOKEN, "Invalid or expired token");
        }

        if (userOpt.isPresent()) {
            UserBO user = userOpt.get();
            if (isExpired(user.getResetTokenExpiry())) {
                return ResponseUtils.failure(ErrorCodes.EC_TOKEN_EXPIRED);
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepo.save(user);
        } else if (sellerOpt.isPresent()) {
            SellerBO seller = sellerOpt.get();
            if (isExpired(seller.getResetTokenExpiry())) {
                return ResponseUtils.failure(ErrorCodes.EC_TOKEN_EXPIRED);
            }
            seller.setPassword(passwordEncoder.encode(newPassword));
            seller.setResetToken(null);
            seller.setResetTokenExpiry(null);
            sellerRepo.save(seller);
        } else {
            AdminBO admin = adminOpt.get();
            if (isExpired(admin.getResetTokenExpiry())) {
                return ResponseUtils.failure(ErrorCodes.EC_TOKEN_EXPIRED);
            }
            admin.setPassword(passwordEncoder.encode(newPassword));
            admin.setResetToken(null);
            admin.setResetTokenExpiry(null);
            adminRepo.save(admin);
        }

        return ResponseUtils.success("Password reset successfully");
    }

    private boolean isExpired(LocalDateTime expiry) {
        return expiry == null || expiry.isBefore(LocalDateTime.now());
    }

    // ============================================================
    // LOGOUT (User + Seller + Admin)
    // ============================================================
    @Transactional
    @Override
    public BaseRs logout() {

        Long id = LoggedUserContext.getLoggedUserId();
        String role = LoggedUserContext.getLoggedUserRole();

        if ("USER".equalsIgnoreCase(role)) {
            userRepo.findById(id).ifPresent(u -> {
                u.setLoggedIn(false);
                userRepo.save(u);
            });
        } else if ("SELLER".equalsIgnoreCase(role)) {
            sellerRepo.findById(id).ifPresent(s -> {
                s.setLogin(false);
                sellerRepo.save(s);
            });
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            adminRepo.findById(id).ifPresent(a -> {
                a.setLogin(false);
                adminRepo.save(a);
            });
        }

        LoggedUserContext.clear();
        SecurityContextHolder.clearContext();

        return ResponseUtils.success(MessageCodes.MC_LOGOUT_SUCCESSFUL);
    }

    // ============================================================
    // VERIFY EMAIL (User + Seller + Admin)
    // ============================================================
    @Override
    @Transactional
    public BaseRs verifyEmail(String token, String email) {

        String cleanEmail = email.trim().toLowerCase();

        Optional<UserBO> userOpt = userRepo.findByEmail(cleanEmail);
        Optional<SellerBO> sellerOpt = Optional.empty();
        Optional<AdminBO> adminOpt = Optional.empty();

        if (userOpt.isEmpty()) {
            sellerOpt = sellerRepo.findByEmailIgnoreCase(cleanEmail);
        }
        if (userOpt.isEmpty() && sellerOpt.isEmpty()) {
            adminOpt = adminRepo.findByEmailIgnoreCase(cleanEmail);
        }

        if (userOpt.isEmpty() && sellerOpt.isEmpty() && adminOpt.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND, "Email not found");
        }

        // USER
        if (userOpt.isPresent()) {
            UserBO user = userOpt.get();

            if (!token.equals(user.getVerificationToken())) {
                return ResponseUtils.failure(ErrorCodes.EC_INVALID_TOKEN, "Invalid token");
            }
            if (isExpired(user.getVerificationTokenExpiry())) {
                return ResponseUtils.failure(ErrorCodes.EC_TOKEN_EXPIRED, "Verification link expired");
            }

            if (user.getPendingEmail() != null && !user.getPendingEmail().isBlank()) {
                user.setEmail(user.getPendingEmail().toLowerCase());
                user.setPendingEmail(null);
            }

            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            if (!user.isActive()) {
                user.setActive(true);
            }

            userRepo.save(user);
            return ResponseUtils.success("Email verified successfully.");
        }

        // SELLER
        if (sellerOpt.isPresent()) {
            SellerBO seller = sellerOpt.get();

            if (!token.equals(seller.getVerificationToken())) {
                return ResponseUtils.failure(ErrorCodes.EC_INVALID_TOKEN, "Invalid token");
            }
            if (isExpired(seller.getVerificationTokenExpiry())) {
                return ResponseUtils.failure(ErrorCodes.EC_TOKEN_EXPIRED, "Verification link expired");
            }

            seller.setEmailVerified(true);
            seller.setLocked(true);  // still locked until admin approves
            seller.setVerificationToken(null);
            seller.setVerificationTokenExpiry(null);

            sellerRepo.save(seller);

            emailService.sendSellerUnderReviewEmail(
                    seller.getEmail(),
                    seller.getFullName()
            );

            return ResponseUtils.success("Seller email verified. Account under review.");
        }

        // ADMIN
        AdminBO admin = adminOpt.get();

        if (!token.equals(admin.getVerificationToken())) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_TOKEN, "Invalid token");
        }
        if (isExpired(admin.getVerificationTokenExpiry())) {
            return ResponseUtils.failure(ErrorCodes.EC_TOKEN_EXPIRED, "Verification link expired");
        }

        admin.setEmailVerified(true);
        admin.setActive(true);
        admin.setVerificationToken(null);
        admin.setVerificationTokenExpiry(null);

        adminRepo.save(admin);

        return ResponseUtils.success("Admin email verified successfully.");
    }

    // ============================================================
    // RESEND VERIFICATION EMAIL (User + Seller + Admin)
    // ============================================================
    @Override
    @Transactional
    public BaseRs resendVerificationEmail(String email) {

        String cleanEmail = email.trim().toLowerCase();

        Optional<UserBO> userOpt = userRepo.findByEmail(cleanEmail);
        Optional<SellerBO> sellerOpt = Optional.empty();
        Optional<AdminBO> adminOpt = Optional.empty();

        if (userOpt.isEmpty()) {
            sellerOpt = sellerRepo.findByEmailIgnoreCase(cleanEmail);
        }
        if (userOpt.isEmpty() && sellerOpt.isEmpty()) {
            adminOpt = adminRepo.findByEmailIgnoreCase(cleanEmail);
        }

        if (userOpt.isEmpty() && sellerOpt.isEmpty() && adminOpt.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND, "Email not registered");
        }

        String newToken = UUID.randomUUID().toString();

        // USER
        if (userOpt.isPresent()) {
            UserBO user = userOpt.get();

            if (user.getEmailVerified()) {
                return ResponseUtils.failure(ErrorCodes.EC_EMAIL_ALREADY_VERIFIED, "Email already verified.");
            }

            user.setVerificationToken(newToken);
            user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
            userRepo.save(user);

            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), newToken);

            return ResponseUtils.success("Verification email sent.");
        }

        // SELLER
        if (sellerOpt.isPresent()) {
            SellerBO seller = sellerOpt.get();

            if (seller.isEmailVerified()) {
                return ResponseUtils.failure(ErrorCodes.EC_EMAIL_ALREADY_VERIFIED, "Email already verified.");
            }

            seller.setVerificationToken(newToken);
            seller.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
            sellerRepo.save(seller);

            emailService.sendVerificationEmail(seller.getEmail(), seller.getFullName(), newToken);

            return ResponseUtils.success("Verification email sent.");
        }

        // ADMIN
        AdminBO admin = adminOpt.get();

        if (admin.isEmailVerified()) {
            return ResponseUtils.failure(ErrorCodes.EC_EMAIL_ALREADY_VERIFIED, "Email already verified.");
        }

        admin.setVerificationToken(newToken);
        admin.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        adminRepo.save(admin);

        emailService.sendVerificationEmail(admin.getEmail(), admin.getFullName(), newToken);

        return ResponseUtils.success("Verification email sent.");
    }
}
