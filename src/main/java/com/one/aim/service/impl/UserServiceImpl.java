package com.one.aim.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.one.aim.rq.UpdateRq;
import com.one.aim.service.EmailService;
import com.one.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.UserBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.helper.UserHelper;
import com.one.aim.mapper.UserMapper;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.repo.UserSessionRepo;
import com.one.aim.rq.UserRq;
import com.one.aim.rs.UserRs;
import com.one.aim.rs.data.UserDataRs;
import com.one.aim.rs.data.UserDataRsList;
import com.one.aim.service.FileService;
import com.one.aim.service.UserService;
import com.one.security.jwt.JwtUtils;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final UserSessionRepo userSessionRepo;
    private final SellerRepo sellerRepo;
    private final AdminRepo adminRepo;
    private final FileService fileService;

    // ===========================================================
    // USER SIGN-UP
    // ===========================================================
    @Override
    @Transactional
    public BaseRs saveUser(UserRq rq) throws Exception {

        // Validate input
        List<String> errors = UserHelper.validateUser(rq);
        if (!errors.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
        }

        String email = rq.getEmail().trim().toLowerCase();

        if (adminRepo.findByEmailIgnoreCase(email).isPresent()
                || userRepo.findByEmailIgnoreCase(email).isPresent()
                || sellerRepo.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseUtils.failure("EMAIL_EXISTS", "Email already registered with another account.");
        }


        // Create new user
        UserBO user = new UserBO();
        user.setFullName(rq.getFullName());
        user.setEmail(email);
        user.setPhoneNo(Utils.getValidString(rq.getPhoneNo()));
        user.setRole("USER");
        user.setEmailVerified(false);   // must verify email
        user.setActive(false);          // inactive until email verification
        user.setLogin(false);

        // Password
        if (Utils.isNotEmpty(rq.getPassword())) {
            user.setPassword(passwordEncoder.encode(rq.getPassword()));
        }

        // Profile image
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            user.setImage(rq.getImage().getBytes());
        }

        // Create verification token
        String token = TokenUtils.generateVerificationToken();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(TokenUtils.generateExpiry());

        userRepo.save(user);

        // Send verification email
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                token
        );

        return ResponseUtils.success(
                new UserDataRs(
                        MessageCodes.MC_SAVED_SUCCESSFUL,
                        UserMapper.mapToUserRs(user)
                )
        );
    }


    // ===========================================================
    // RETRIEVE CURRENT USER
    // ===========================================================
    @Override
    public BaseRs retrieveUser() {
        try {
            Long userId = AuthUtils.findLoggedInUser().getDocId();

            UserBO user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

            UserRs userRs = UserMapper.mapToUserRs(user);

            return ResponseUtils.success(
                    new UserDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, userRs)
            );

        } catch (Exception e) {
            log.error("retrieveUser() error ->", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // RETRIEVE ALL USERS (ADMIN)
    // ===========================================================
    @Override
    public BaseRs retrieveAllUser() {

        try {
            // Admin only
            if (adminRepo.findById(AuthUtils.findLoggedInUser().getDocId()).isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
            }

            List<UserBO> users = userRepo.findAll();
            if (Utils.isEmpty(users)) {
                return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
            }

            List<UserRs> userRsList = UserMapper.mapToUserRsList(users);

            return ResponseUtils.success(
                    new UserDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, userRsList)
            );

        } catch (Exception e) {
            log.error("retrieveAllUser() error ->", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }


    // ===========================================================
    // DELETE USER (ADMIN ONLY)
    // ===========================================================
    @Override
    @Transactional
    public BaseRs deleteUser(String id) {

        try {
            // Admin validation
            if (adminRepo.findById(AuthUtils.findLoggedInUser().getDocId()).isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
            }

            UserBO user = userRepo.findById(Long.valueOf(id))
                    .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

            userRepo.delete(user);

            return ResponseUtils.success(
                    new UserDataRs(MessageCodes.MC_DELETED_SUCCESSFUL,
                            UserMapper.mapToUserRs(user))
            );

        } catch (Exception e) {
            log.error("deleteUser() error ->", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // UPDATE USER PROFILE
    // ===========================================================
    @Override
    @Transactional
    public BaseRs updateUserProfile(String email, UpdateRq rq) {

        UserBO user = userRepo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean updated = false;

        // -------------------------
        // FULL NAME
        // -------------------------
        if (Utils.isNotEmpty(rq.getFullName())) {
            user.setFullName(rq.getFullName());
            updated = true;
        }

        // -------------------------
        // PHONE NUMBER
        // -------------------------
        if (Utils.isNotEmpty(rq.getPhoneNo())) {

            if (!rq.getPhoneNo().matches("^[6-9]\\d{9}$")) {
                return ResponseUtils.failure("EC_INVALID_PHONE", "Invalid mobile number.");
            }

            user.setPhoneNo(rq.getPhoneNo());
            updated = true;
        }

        // -------------------------
        // PROFILE IMAGE
        // -------------------------
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            try {
                user.setImage(rq.getImage().getBytes());
            } catch (Exception e) {
                return ResponseUtils.failure("EC_INVALID_IMAGE", "Unable to process the image.");
            }
            updated = true;
        }

        // -------------------------
        // PASSWORD CHANGE
        // -------------------------
        if (rq.hasPasswordUpdate()) {

            if (!rq.isPasswordDataValid()) {
                return ResponseUtils.failure("EC_INVALID_PASSWORD", rq.passwordErrorMessage());
            }

            if (!passwordEncoder.matches(rq.getOldPassword(), user.getPassword())) {
                return ResponseUtils.failure("EC_INVALID_PASSWORD", "Old password is incorrect.");
            }

            user.setPassword(passwordEncoder.encode(rq.getNewPassword()));
            updated = true;
        }

        // -------------------------
        // EMAIL CHANGE → NEEDS VERIFICATION
        // -------------------------
        if (Utils.isNotEmpty(rq.getEmail()) &&
                !rq.getEmail().equalsIgnoreCase(user.getEmail())) {

            String newEmail = rq.getEmail().toLowerCase();

            // Duplicate check
            if (userRepo.findByEmail(newEmail).isPresent()) {
                return ResponseUtils.failure("EMAIL_ALREADY_IN_USE", "Email already registered.");
            }

            // New verification token
            String token = TokenUtils.generateVerificationToken();

            user.setPendingEmail(newEmail);
            user.setVerificationToken(token);
            user.setVerificationTokenExpiry(TokenUtils.generateExpiry());

            userRepo.save(user);

            emailService.sendVerificationEmail(
                    newEmail,
                    user.getFullName(),
                    token
            );

            return ResponseUtils.success("Verification email sent. Please verify to complete email update.");
        }

        // -------------------------
        // Save updates
        // -------------------------
        if (updated) {
            userRepo.save(user);
            return ResponseUtils.success("Profile updated successfully.");
        }

        return ResponseUtils.failure("NO_CHANGES", "No valid fields provided to update.");
    }


    // ===========================================================
    // VERIFY EMAIL (for signup + email update)
    // ===========================================================
    @Override
    @Transactional
    public BaseRs verifyEmail(String token) {

        UserBO user = userRepo.findByVerificationToken(token)
                .orElse(null);

        if (user == null) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_TOKEN, "Invalid verification token.");
        }

        if (user.getVerificationTokenExpiry() == null ||
                user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseUtils.failure("TOKEN_EXPIRED", "Verification token has expired.");
        }

        // If this verification was for an email update
        if (Utils.isNotEmpty(user.getPendingEmail())
                && !user.getPendingEmail().equalsIgnoreCase(user.getEmail())) {
            user.setEmail(user.getPendingEmail());
            user.setPendingEmail(null);
        }

        // Mark email verified
        user.setEmailVerified(true);
        user.setActive(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);

        userRepo.save(user);

        return ResponseUtils.success("Email verified successfully.");
    }
}
