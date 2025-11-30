package com.one.aim.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.one.aim.bo.FileBO;
import com.one.aim.rq.UpdateRq;
import com.one.aim.rs.data.UserDataRs;
import com.one.aim.rq.UserRq;
import com.one.aim.service.EmailService;
import com.one.utils.PhoneUtils;
import com.one.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.one.aim.bo.UserBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.helper.UserHelper;
import com.one.aim.mapper.UserMapper;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.repo.UserSessionRepo;
import com.one.aim.rs.UserRs;
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

        List<String> errors = UserHelper.validateUser(rq);
        if (!errors.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
        }

        String email = rq.getEmail().trim().toLowerCase();

        if (adminRepo.findByEmailIgnoreCase(email).isPresent()
                || userRepo.findByEmailIgnoreCase(email).isPresent()
                || sellerRepo.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseUtils.failure("EMAIL_EXISTS", "Email already registered.");
        }

        String phone = PhoneUtils.normalize(rq.getPhoneNo());
        if (userRepo.existsByPhoneNo(phone) ||
                sellerRepo.existsByPhoneNo(phone) ||
                adminRepo.existsByPhoneNo(phone)) {
            return ResponseUtils.failure("PHONE_EXISTS", "Phone number already exists.");
        }

        UserBO user = new UserBO();
        user.setFullName(rq.getFullName());
        user.setEmail(email);
        user.setPhoneNo(phone);
        user.setRole("USER");
        user.setEmailVerified(false);
        user.setActive(false);
        user.setLoggedIn(false);

        if (Utils.isNotEmpty(rq.getPassword())) {
            user.setPassword(passwordEncoder.encode(rq.getPassword()));
        }

        // NO IMAGE HANDLING FOR SIGNUP NOW

        String token = TokenUtils.generateVerificationToken();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(TokenUtils.generateExpiry());

        userRepo.save(user);

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
    // CURRENT USER
    // ===========================================================
    @Override
    public BaseRs retrieveUser() {
        try {
            Long id = AuthUtils.findLoggedInUser().getDocId();

            UserBO user = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

            return ResponseUtils.success(
                    new UserDataRs(
                            MessageCodes.MC_RETRIEVED_SUCCESSFUL,
                            UserMapper.mapToUserRs(user)
                    )
            );

        } catch (Exception e) {
            log.error("retrieveUser() error ->", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // ADMIN – ALL USERS
    // ===========================================================
    @Override
    public BaseRs retrieveAllUser() {
        try {
            Long adminId = AuthUtils.findLoggedInUser().getDocId();
            if (adminRepo.findById(adminId).isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
            }

            List<UserBO> users = userRepo.findAll();
            if (Utils.isEmpty(users)) {
                return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
            }

            return ResponseUtils.success(
                    new UserDataRsList(
                            MessageCodes.MC_RETRIEVED_SUCCESSFUL,
                            UserMapper.mapToUserRsList(users)
                    )
            );

        } catch (Exception e) {
            log.error("retrieveAllUser() error ->", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }


    // ===========================================================
    // ADMIN – DELETE USER
    // ===========================================================
    @Override
    @Transactional
    public BaseRs deleteUser(String id) {

        Long loginId = AuthUtils.findLoggedInUser().getDocId();

        if (adminRepo.findById(loginId).isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
        }

        UserBO user = userRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

        if (!user.isActive()) {
            return ResponseUtils.failure("USER_ALREADY_INACTIVE", "User already deleted.");
        }

        // Soft delete & free email
        String oldEmail = user.getEmail();

        user.setActive(false);
        user.setDeleted(true);
        user.setLoggedIn(false);
        user.setEmailVerified(false);
        user.setPendingEmail(null);
        user.setEmail(oldEmail + "_DELETED_" + user.getId());

        userRepo.save(user);

        return ResponseUtils.success(
                new UserDataRs(MessageCodes.MC_DELETED_SUCCESSFUL, UserMapper.mapToUserRs(user))
        );
    }


    @Override
    @Transactional
    public BaseRs updateUserProfile(String email, UpdateRq rq) {

        UserBO user = userRepo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean updated = false;

        // -----------------------------
        // FULL NAME
        // -----------------------------
        if (Utils.isNotEmpty(rq.getFullName())) {
            user.setFullName(rq.getFullName());
            updated = true;
        }

        // -----------------------------
        // PHONE NUMBER
        // -----------------------------
        if (Utils.isNotEmpty(rq.getPhoneNo())) {

            String normalized = PhoneUtils.normalize(rq.getPhoneNo());

            if (!normalized.matches("^[6-9]\\d{9}$"))
                return ResponseUtils.failure("EC_INVALID_PHONE", "Invalid mobile number.");

            if (!normalized.equals(user.getPhoneNo())) {

                boolean exists =
                        userRepo.existsByPhoneNo(normalized) ||
                                sellerRepo.existsByPhoneNo(normalized) ||
                                adminRepo.existsByPhoneNo(normalized);

                if (exists)
                    return ResponseUtils.failure("PHONE_EXISTS", "Phone already registered.");

                user.setPhoneNo(normalized);
                updated = true;
            }
        }

        // -----------------------------
        // REMOVE IMAGE
        // -----------------------------
        if (Boolean.TRUE.equals(rq.getRemoveImage())) {

            if (user.getImageFileId() != null) {
                try {
                    fileService.deleteFile(user.getImageFileId());
                } catch (Exception ignore) {}

                user.setImageFileId(null);
                updated = true;
            }
        }

        // -----------------------------
        // UPLOAD IMAGE (REPLACE)
        // -----------------------------
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {

            try {
                // delete old image
                if (user.getImageFileId() != null) {
                    try { fileService.deleteFile(user.getImageFileId()); }
                    catch (Exception ignore) {}
                }

                FileBO uploaded = fileService.uploadAndReturnFile(rq.getImage());
                user.setImageFileId(uploaded.getId());

            } catch (Exception e) {
                return ResponseUtils.failure("EC_INVALID_IMAGE", "Failed to upload image.");
            }

            updated = true;
        }

        // -----------------------------
        // PASSWORD UPDATE
        // -----------------------------
        if (rq.hasPasswordUpdate()) {

            if (!rq.isPasswordDataValid())
                return ResponseUtils.failure("EC_INVALID_PASSWORD", rq.passwordErrorMessage());

            if (!passwordEncoder.matches(rq.getOldPassword(), user.getPassword()))
                return ResponseUtils.failure("EC_INVALID_PASSWORD", "Old password incorrect.");

            user.setPassword(passwordEncoder.encode(rq.getNewPassword()));
            updated = true;
        }

        // -----------------------------
        // EMAIL CHANGE (with verification)
        // -----------------------------
        if (Utils.isNotEmpty(rq.getEmail()) &&
                !rq.getEmail().equalsIgnoreCase(user.getEmail())) {

            String newEmail = rq.getEmail().trim().toLowerCase();

            boolean exists =
                    userRepo.findByEmailIgnoreCase(newEmail).isPresent() ||
                            sellerRepo.findByEmailIgnoreCase(newEmail).isPresent() ||
                            adminRepo.findByEmailIgnoreCase(newEmail).isPresent();

            if (exists)
                return ResponseUtils.failure("EMAIL_ALREADY_IN_USE", "Email already registered.");

            emailService.initiateUserEmailChange(user, newEmail);
            userRepo.save(user);

            return ResponseUtils.success("Verification email sent for new email.");
        }

        // -----------------------------
        // SAVE IF CHANGED
        // -----------------------------
        if (updated) {
            userRepo.save(user);
            return ResponseUtils.success("Profile updated successfully.");
        }

        return ResponseUtils.failure("NO_CHANGES", "No valid fields provided.");
    }



    // ===========================================================
    // USER – DELETE OWN ACCOUNT
    // ===========================================================
    @Override
    @Transactional
    public BaseRs deleteMyAccount() throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        UserBO user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

        if (!user.isActive()) {
            return ResponseUtils.failure("USER_ALREADY_INACTIVE", "Account already deleted.");
        }

        user.setActive(false);
        user.setLoggedIn(false);
        user.setEmailVerified(false);

        user.setPendingEmail(null);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepo.save(user);

        return ResponseUtils.success(
                new UserDataRs("Your account has been deleted successfully.")
        );
    }

}
