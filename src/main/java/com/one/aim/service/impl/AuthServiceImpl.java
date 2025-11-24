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
import com.one.security.jwt.JwtUtils;
import com.one.service.impl.UserDetailsImpl;
import com.one.utils.AuthUtils;
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
    private final EmailService emailService;   // All email logic centralized here

    // ============================================================
    // SIGN IN (User + Seller + Admin)
    // ============================================================
    @Override
    public BaseRs signIn(Authentication authentication) throws Exception {

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        // CENTRALIZED EMAIL VERIFIED CHECK
        emailService.checkEmailVerified(user);

        // SELLER LOGIN CHECKS
        if ("SELLER".equals(user.getRole())) {

            SellerBO seller = sellerRepo.findById(user.getId()).orElse(null);
            if (seller == null) {
                return ResponseUtils.failure("SELLER_NOT_FOUND");
            }

            if (seller.isLocked()) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_ACCOUNT_LOCKED,
                        "Your seller account has been locked by admin."
                );
            }
        }

        // ADMIN LOGIN CHECKS
        if ("ADMIN".equals(user.getRole())) {

            AdminBO admin = adminRepo.findById(user.getId()).orElse(null);

            if (admin == null) {
                return ResponseUtils.failure("ADMIN_NOT_FOUND");
            }
        }

        // GENERATE TOKENS
        String accessToken = jwtUtils.generateAccessToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        LoginDataRs rs = new LoginDataRs(MessageCodes.MC_LOGIN_SUCCESSFUL);
        rs.setAccessToken(accessToken);
        rs.setRefreshToken(refreshToken);

        // ----------------------------------------------------
        // CUSTOM RESPONSE FOR SELLER LOGIN
        // ----------------------------------------------------
        if ("SELLER".equals(user.getRole())) {
            SellerBO seller = sellerRepo.findById(user.getId()).orElse(null);

            rs.setSellerId(seller.getSellerId());  // <-------- SELLER ID FIELD
            rs.setUsername(seller.getEmail());
            rs.setFullname(seller.getFullName());
            rs.setEmail(seller.getEmail());
            rs.setRole("SELLER");

            return ResponseUtils.success(rs);
        }

        // ----------------------------------------------------
        // NORMAL RESPONSE for USER and ADMIN
        // ----------------------------------------------------
        rs.setEmpId(user.getId());
        rs.setUsername(user.getEmail());
        rs.setFullname(user.getFullName());
        rs.setEmail(user.getEmail());
        rs.setRole(user.getRole());

        return ResponseUtils.success(rs);
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

        if (userOpt.isEmpty()) sellerOpt = sellerRepo.findByResetToken(token);
        if (userOpt.isEmpty() && sellerOpt.isEmpty()) adminOpt = adminRepo.findByResetToken(token);

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
    // LOGOUT
    // ============================================================
    @Transactional
    @Override
    public BaseRs logout() {

        Long id = AuthUtils.getLoggedUserId();
        String role = AuthUtils.getLoggedUserRole();

        if (id == null) {
            return ResponseUtils.failure("NOT_LOGGED_IN", "No active session found.");
        }

        if ("USER".equalsIgnoreCase(role)) {
            userRepo.findById(id).ifPresent(u -> {
                u.setLogin(false);
                userRepo.save(u);
            });
        }

        if ("SELLER".equalsIgnoreCase(role)) {
            sellerRepo.findById(id).ifPresent(s -> {
                s.setLogin(false);
                sellerRepo.save(s);
            });
        }

        if ("ADMIN".equalsIgnoreCase(role)) {
            adminRepo.findById(id).ifPresent(a -> {
                a.setLogin(false);
                adminRepo.save(a);
            });
        }

        SecurityContextHolder.clearContext();

        return ResponseUtils.success(MessageCodes.MC_LOGOUT_SUCCESSFUL);
    }



    // ============================================================
    // VERIFY EMAIL (DELEGATED)
    // ============================================================
    @Override
    @Transactional
    public BaseRs verifyEmail(String token, String email) {
        return emailService.verifyEmail(token, email);
    }


    // ============================================================
    // RESEND VERIFICATION EMAIL (DELEGATED)
    // ============================================================
    @Override
    @Transactional
    public BaseRs resendVerificationEmail(String email) {
        return emailService.resendVerificationEmail(email);
    }
}
