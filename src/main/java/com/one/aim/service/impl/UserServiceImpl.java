package com.one.aim.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.one.aim.bo.FileBO;
import com.one.aim.rq.UpdateRq;
import com.one.aim.rq.UserFilterRequest;
import com.one.aim.rs.UserPageResponse;
import com.one.aim.rs.data.UserDataRsList;
import com.one.aim.service.EmailService;
import com.one.utils.PhoneUtils;
import com.one.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import com.one.aim.rq.UserRq;
import com.one.aim.rs.UserRs;
import com.one.aim.rs.data.UserDataRs;
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

        // GLOBAL EMAIL CHECK
        if (adminRepo.findByEmailIgnoreCase(email).isPresent()
                || userRepo.findByEmailIgnoreCase(email).isPresent()
                || sellerRepo.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseUtils.failure("EMAIL_EXISTS", "Email already registered with another account.");
        }

        // Phone
        String phone = PhoneUtils.normalize(rq.getPhoneNo());
        if (userRepo.existsByPhoneNo(phone) ||
                sellerRepo.existsByPhoneNo(phone) ||
                adminRepo.existsByPhoneNo(phone)) {
            return ResponseUtils.failure("PHONE_EXISTS", "Phone number already registered.");
        }

        // Create new user
        UserBO user = new UserBO();
        user.setFullName(rq.getFullName());
        user.setEmail(email);
        user.setPhoneNo(phone);
        user.setRole("USER");
        user.setEmailVerified(false);
        user.setActive(false);
        user.setLoggedIn(false);

        // Password
        if (Utils.isNotEmpty(rq.getPassword())) {
            user.setPassword(passwordEncoder.encode(rq.getPassword()));
        }

        // Profile image
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            FileBO uploaded = fileService.uploadAndReturnFile(rq.getImage());
            user.setImageFileId(uploaded.getId());
        }

        // --------------------------------------------------------
        // EMAIL VERIFICATION (Correct centralized logic)
        // --------------------------------------------------------
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

        Long loginId = AuthUtils.findLoggedInUser().getDocId();

        boolean isAdmin = adminRepo.findById(loginId).isPresent();
        if (!isAdmin) {
            return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
        }

        UserBO user = userRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

        if (!user.isActive()) {
            return ResponseUtils.failure("USER_ALREADY_INACTIVE", "User already deleted.");
        }

        // ================================
        // SOFT DELETE + FREE THE EMAIL
        // ================================
        String oldEmail = user.getEmail();
        user.setActive(false);
        user.setDeleted(true);
        user.setLoggedIn(false);
        user.setEmailVerified(false);

        // free email so user can register again
        user.setEmail(oldEmail + "_DELETED_" + user.getId());

        userRepo.save(user);

        return ResponseUtils.success(
                new UserDataRs(MessageCodes.MC_DELETED_SUCCESSFUL, UserMapper.mapToUserRs(user))
        );
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

        // Full name
        if (Utils.isNotEmpty(rq.getFullName())) {
            user.setFullName(rq.getFullName());
            updated = true;
        }

        // Phone number
        if (Utils.isNotEmpty(rq.getPhoneNo())) {

            String normalized = PhoneUtils.normalize(rq.getPhoneNo());

            if (!normalized.matches("^[6-9]\\d{9}$")) {
                return ResponseUtils.failure("EC_INVALID_PHONE", "Invalid mobile number.");
            }

            if (!normalized.equals(user.getPhoneNo())) {

                boolean exists =
                        userRepo.existsByPhoneNo(normalized) ||
                                sellerRepo.existsByPhoneNo(normalized) ||
                                adminRepo.existsByPhoneNo(normalized);

                if (exists) {
                    return ResponseUtils.failure("PHONE_EXISTS", "Phone number already registered.");
                }

                user.setPhoneNo(normalized);
                updated = true;
            }
        }

        // Image
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            try {
                FileBO uploaded = fileService.uploadAndReturnFile(rq.getImage());
                user.setImageFileId(uploaded.getId());
            } catch (Exception e) {
                return ResponseUtils.failure("EC_INVALID_IMAGE", "Unable to upload profile image.");
            }
            updated = true;
        }

        // Password update
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


        // ---------------------------------------------------------
        // EMAIL CHANGE (shifted to EmailService)
        // ---------------------------------------------------------
        if (Utils.isNotEmpty(rq.getEmail()) &&
                !rq.getEmail().equalsIgnoreCase(user.getEmail())) {

            String newEmail = rq.getEmail().trim().toLowerCase();

            boolean emailExists =
                    userRepo.findByEmailIgnoreCase(newEmail).isPresent() ||
                            sellerRepo.findByEmailIgnoreCase(newEmail).isPresent() ||
                            adminRepo.findByEmailIgnoreCase(newEmail).isPresent();

            if (emailExists) {
                return ResponseUtils.failure("EMAIL_ALREADY_IN_USE", "Email already registered.");
            }

            //  CALL EmailService
            emailService.initiateUserEmailChange(user, newEmail);

            userRepo.save(user);

            return ResponseUtils.success("Verification email sent. Please verify your new email.");
        }


        // SAVE CHANGES
        if (updated) {
            userRepo.save(user);
            return ResponseUtils.success("Profile updated successfully.");
        }

        return ResponseUtils.failure("NO_CHANGES", "No valid fields provided.");
    }

    // ===========================================================
// SELF DELETE USER ACCOUNT (USER)
// ===========================================================
    @Override
    @Transactional
    public BaseRs deleteMyAccount() throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        UserBO user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));

        if (!user.isActive()) {
            return ResponseUtils.failure(
                    "USER_ALREADY_INACTIVE",
                    "Account already deleted."
            );
        }

        // Soft delete
        user.setActive(false);          // disable account
        user.setLoggedIn(false);           // force logout
        user.setEmailVerified(false);   // disable login
        user.setPendingEmail(null);     // clear email change requests
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepo.save(user);

        return ResponseUtils.success(
                new UserDataRs(
                        "Your account has been deleted successfully."
                )
        );
    }

}
