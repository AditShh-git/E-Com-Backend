package com.one.aim.service.impl;

import java.util.List;
import java.util.Optional;

import com.one.aim.bo.FileBO;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.service.EmailService;
import com.one.utils.PhoneUtils;
import com.one.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.one.aim.bo.AdminBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.helper.AdminHelper;
import com.one.aim.mapper.AdminMapper;
import com.one.aim.repo.AdminRepo;
import com.one.aim.rq.AdminRq;
import com.one.aim.rs.AdminRs;
import com.one.aim.rs.data.AdminDataRs;
import com.one.aim.service.AdminService;
import com.one.aim.service.FileService;
import com.one.constants.StringConstants;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepo adminRepo;
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final FileService fileService;

    // =========================================================
    // CREATE ADMIN (with email verification)
    // =========================================================
    @Override
    @Transactional
    public BaseRs createAdmin(AdminRq rq) throws Exception {

        log.debug("Executing createAdmin()");

        // VALIDATION
        List<String> errors = AdminHelper.validateAdmin(rq);
        if (Utils.isNotEmpty(errors)) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
        }

        String email = rq.getEmail().trim().toLowerCase();

        // EMAIL UNIQUE CHECK
        if (adminRepo.findByEmailIgnoreCase(email).isPresent()
                || userRepo.findByEmailIgnoreCase(email).isPresent()
                || sellerRepo.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseUtils.failure("EMAIL_EXISTS", "Email already registered with another account.");
        }

        // PHONE CHECK
        String normalizedPhone = PhoneUtils.normalize(rq.getPhoneNo());
        if (!normalizedPhone.matches("^[6-9]\\d{9}$")) {
            return ResponseUtils.failure("INVALID_PHONE", "Invalid mobile number.");
        }

        boolean phoneExists =
                adminRepo.existsByPhoneNo(normalizedPhone) ||
                        userRepo.existsByPhoneNo(normalizedPhone) ||
                        sellerRepo.existsByPhoneNo(normalizedPhone);

        if (phoneExists) {
            return ResponseUtils.failure("PHONE_EXISTS", "Phone number already registered.");
        }

        // CREATE ADMIN OBJECT
        AdminBO adminBO = new AdminBO();
        adminBO.setRole("ADMIN");
        adminBO.setFullName(Utils.getValidString(rq.getUserName()));
        adminBO.setEmail(email);
        adminBO.setPhoneNo(normalizedPhone);
        adminBO.setPassword(passwordEncoder.encode(Utils.getValidString(rq.getPassword())));

        // IMAGE UPLOAD
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            FileBO uploaded = fileService.uploadAndReturnFile(rq.getImage());
            adminBO.setImageId(uploaded.getId());
        }

        // -----------------------------------------
        // EMAIL VERIFICATION (Correct centralized logic)
        // -----------------------------------------
        String token = TokenUtils.generateVerificationToken();
        adminBO.setVerificationToken(token);
        adminBO.setVerificationTokenExpiry(TokenUtils.generateExpiry());
        adminBO.setEmailVerified(false);
        adminBO.setActive(false);

        adminRepo.save(adminBO);

        // SEND VERIFICATION EMAIL
        emailService.sendVerificationEmail(
                adminBO.getEmail(),
                adminBO.getFullName(),
                token
        );

        return ResponseUtils.success(
                new AdminDataRs(
                        MessageCodes.MC_SAVED_SUCCESSFUL,
                        AdminMapper.mapToAdminRs(adminBO)
                )
        );
    }




    // =========================================================
    // UPDATE ADMIN PROFILE
    // =========================================================
    @Override
    @Transactional
    public BaseRs updateAdmin(AdminRq rq) throws Exception {

        log.debug("Executing updateAdmin()");

        String docId = Utils.getValidString(rq.getDocId());
        if (Utils.isEmpty(docId)) {
            return ResponseUtils.failure(ErrorCodes.EC_REQUIRED_DOCID);
        }

        AdminBO adminBO = adminRepo.findById(Long.parseLong(docId))
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_ADMIN_NOT_FOUND));

        boolean updated = false;

        // -------------------------------------------------
        // FULL NAME
        // -------------------------------------------------
        if (Utils.isNotEmpty(rq.getUserName())) {
            adminBO.setFullName(rq.getUserName());
            updated = true;
        }

        // -------------------------------------------------
        // PHONE (normalize + uniqueness across all accounts)
        // -------------------------------------------------
        if (Utils.isNotEmpty(rq.getPhoneNo())) {

            String normalized = PhoneUtils.normalize(rq.getPhoneNo());

            if (!normalized.matches("^[6-9]\\d{9}$")) {
                return ResponseUtils.failure("INVALID_PHONE", "Invalid mobile number.");
            }

            if (!normalized.equals(adminBO.getPhoneNo())) {

                boolean exists =
                        adminRepo.existsByPhoneNo(normalized) ||
                                userRepo.existsByPhoneNo(normalized) ||
                                sellerRepo.existsByPhoneNo(normalized);

                if (exists) {
                    return ResponseUtils.failure("PHONE_EXISTS", "Phone number already registered.");
                }

                adminBO.setPhoneNo(normalized);
                updated = true;
            }
        }

        // -------------------------------------------------
        // PASSWORD
        // -------------------------------------------------
        if (Utils.isNotEmpty(rq.getPassword())) {
            adminBO.setPassword(passwordEncoder.encode(rq.getPassword()));
            updated = true;
        }

        // -------------------------------------------------
        // IMAGE
        // -------------------------------------------------
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {

            FileBO uploaded = fileService.uploadAndReturnFile(rq.getImage());

            adminBO.setImageId(uploaded.getId());
            updated = true;
        }

        // -------------------------------------------------
        // SAVE ONLY IF ANYTHING CHANGED
        // -------------------------------------------------
        if (updated) {
            adminRepo.save(adminBO);
            return ResponseUtils.success(
                    new AdminDataRs(
                            MessageCodes.MC_UPDATED_SUCCESSFUL,
                            AdminMapper.mapToAdminRs(adminBO)
                    )
            );
        }

        return ResponseUtils.failure("NO_CHANGES", "No valid fields provided to update.");
    }


    // =========================================================
    // GET LOGGED-IN ADMIN PROFILE
    // =========================================================
    @Override
    public BaseRs retrieveAdmin() throws Exception {

        log.debug("Executing retrieveAdmin()");

        Long loggedId = AuthUtils.findLoggedInUser().getDocId();
        AdminBO adminBO = adminRepo.findById(loggedId)
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_ADMIN_NOT_FOUND));

        return ResponseUtils.success(
                new AdminDataRs(
                        MessageCodes.MC_RETRIEVED_SUCCESSFUL,
                        AdminMapper.mapToAdminRs(adminBO)
                )
        );
    }

    // =========================================================
    // DELETE ADMIN
    // =========================================================
    @Override
    @Transactional
    public BaseRs deleteAdmin(String id) throws Exception {

        log.debug("Executing deleteAdmin()");

        AdminBO adminBO = adminRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_ADMIN_NOT_FOUND));

        adminRepo.delete(adminBO);

        return ResponseUtils.success(
                new AdminDataRs(
                        MessageCodes.MC_DELETED_SUCCESSFUL,
                        AdminMapper.mapToAdminRs(adminBO)
                )
        );
    }
}
