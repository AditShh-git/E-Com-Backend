package com.one.aim.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.one.aim.rq.UpdateRq;
import com.one.aim.service.EmailService;
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

    // ============================================
    //  REGISTER / UPDATE USER
    // ============================================
    @Override
    @Transactional
    public BaseRs saveUser(UserRq rq) throws Exception {
        log.debug("Executing saveUser() ->");

        List<String> errors = UserHelper.validateUser(rq);
        if (Utils.isNotEmpty(errors)) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
        }

        String email = Utils.getValidString(rq.getEmail());
        String docId = Utils.getValidString(rq.getDocId());

        Optional<UserBO> existingUserOpt = userRepo.findByEmail(email);
        if (existingUserOpt.isPresent()) {
            UserBO existingUser = existingUserOpt.get();

            if (Utils.isEmpty(docId)) {
                return ResponseUtils.failure("USER_ALREADY_EXISTS");
            }

            long currentUserId = Long.parseLong(docId);
            if (!existingUser.getId().equals(currentUserId)) {
                return ResponseUtils.failure("EMAIL_ALREADY_IN_USE");
            }
        }

        UserBO userBO;
        String message;

        if (Utils.isNotEmpty(docId)) { // UPDATE
            Optional<UserBO> optUserBO = userRepo.findById(Long.parseLong(docId));
            if (optUserBO.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
            }
            userBO = optUserBO.get();
            message = MessageCodes.MC_UPDATED_SUCCESSFUL;
        } else { // NEW USER
            userBO = new UserBO();
            message = MessageCodes.MC_SAVED_SUCCESSFUL;
        }

        userBO.setEmail(email);
        userBO.setFullName(rq.getFullName());
        userBO.setPhoneNo(Utils.getValidString(rq.getPhoneNo()));
        userBO.setRole("USER");

        // Handle password
        String rawPassword = Utils.getValidString(rq.getPassword());
        String existingEncodedPassword = userBO.getPassword();
        if (Utils.isNotEmpty(rawPassword)) {
            if (existingEncodedPassword == null || !passwordEncoder.matches(rawPassword, existingEncodedPassword)) {
                userBO.setPassword(passwordEncoder.encode(rawPassword));
            }
        }

        // Handle image upload
        try {
            if (rq.getImage() != null && !rq.getImage().isEmpty()) {
                userBO.setImage(rq.getImage().getBytes());
            }
        } catch (IOException e) {
            log.error("Error converting image file to bytes", e);
        }

        userRepo.save(userBO);
        UserRs userRs = UserMapper.mapToUserRs(userBO);
        return ResponseUtils.success(new UserDataRs(message, userRs));
    }

    // ============================================
    //  RETRIEVE CURRENT USER
    // ============================================
    @Override
    public BaseRs retrieveUser() throws Exception {
        log.debug("Executing retrieveUser() ->");
        try {
            Optional<UserBO> optUser = userRepo.findById(AuthUtils.findLoggedInUser().getDocId());
            if (optUser.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
            }
            UserRs userRs = UserMapper.mapToUserRs(optUser.get());
            return ResponseUtils.success(new UserDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, userRs));
        } catch (Exception e) {
            log.error("Exception retrieveUser() ->", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }

    // ============================================
    //  RETRIEVE ALL USERS (ADMIN ONLY)
    // ============================================
    @Override
    public BaseRs retrieveAllUser() throws Exception {
        log.debug("Executing retrieveAllUser() ->");
        try {
            Optional<AdminBO> optAdmin = adminRepo.findById(AuthUtils.findLoggedInUser().getDocId());
            if (optAdmin.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
            }
            List<UserBO> userBOs = userRepo.findAll();
            if (Utils.isEmpty(userBOs)) {
                return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
            }
            List<UserRs> userRsList = UserMapper.mapToUserRsList(userBOs);
            return ResponseUtils.success(new UserDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, userRsList));
        } catch (Exception e) {
            log.error("Exception retrieveAllUser() ->", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }

    // ============================================
    //  DELETE USER (ADMIN)
    // ============================================
    @Override
    @Transactional
    public BaseRs deleteUser(String id) throws Exception {
        log.debug("Executing deleteUser(id) ->");
        try {
            Long userId = Long.valueOf(id);
            Optional<AdminBO> optAdmin = adminRepo.findById(AuthUtils.findLoggedInUser().getDocId());
            if (optAdmin.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
            }
            Optional<UserBO> optUser = userRepo.findById(userId);
            if (optUser.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
            }
            UserBO userBO = optUser.get();
            userRepo.delete(userBO);
            UserRs userRs = UserMapper.mapToUserRs(userBO);
            return ResponseUtils.success(new UserDataRs(MessageCodes.MC_DELETED_SUCCESSFUL, userRs));
        } catch (Exception e) {
            log.error("Exception deleteUser(id) ->", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }

    // ===========================================================
    // UPDATE PROFILE
    // ===========================================================
    @Override
    @Transactional
    public BaseRs updateUserProfile(String email, UpdateRq request) {
        UserBO user = userRepo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean updated = false;

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
            updated = true;
        }

        if (request.getPhoneNo() != null && !request.getPhoneNo().isBlank()) {
            if (!request.getPhoneNo().matches("^[6-9]\\d{9}$")) {
                return ResponseUtils.failure("EC_INVALID_PHONE", "Please provide a valid 10-digit mobile number.");
            }
            user.setPhoneNo(request.getPhoneNo());
            updated = true;
        }

        //  Optional image update
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                user.setImage(request.getImage().getBytes());
                updated = true;
            } catch (IOException e) {
                log.error("Failed to update profile image for user {}: {}", email, e.getMessage());
                return ResponseUtils.failure("EC_INVALID_IMAGE", "Unable to process uploaded image.");
            }
        }

        // 3. Password Change Logic (all fields required)
        if (request.getOldPassword() != null || request.getNewPassword() != null || request.getConfirmPassword() != null) {

            // Check all fields are present
            if (request.getOldPassword() == null || request.getNewPassword() == null || request.getConfirmPassword() == null) {
                return ResponseUtils.failure("EC_INVALID_PASSWORD", "All password fields are required.");
            }

            // Verify old password
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return ResponseUtils.failure("EC_INVALID_PASSWORD", "Old password is incorrect.");
            }

            // Prevent same old and new password
            if (request.getOldPassword().equals(request.getNewPassword())) {
                return ResponseUtils.failure("EC_INVALID_PASSWORD", "New password cannot be same as old password.");
            }

            // Check new password match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseUtils.failure("EC_INVALID_PASSWORD", "New passwords do not match.");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            updated = true;
            log.info("Password updated for user {}", email);
        }

        // 4. Update Email (send verification link)
        if (request.getEmail() != null && !request.getEmail().isBlank() && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            String verificationToken = UUID.randomUUID().toString();
            user.setPendingEmail(request.getEmail().toLowerCase());
            user.setVerificationToken(verificationToken);
            user.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userRepo.save(user);

            emailService.sendVerificationEmail(request.getEmail(), user.getFullName(), verificationToken);

            return ResponseUtils.success("Verification email sent to new address. Verify to update your email.");
        }

        if (updated) {
            userRepo.save(user);
            return ResponseUtils.success("Profile updated successfully");
        }

        return ResponseUtils.failure("NO_CHANGES", "No valid fields provided to update.");
    }

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
}