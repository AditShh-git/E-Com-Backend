package com.one.aim.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.one.aim.bo.FileBO;
import com.one.aim.helper.SellerHelper;
import com.one.aim.repo.UserRepo;
import com.one.aim.rq.SellerFilterRequest;
import com.one.aim.rq.UpdateRq;
import com.one.aim.rs.SellerPageResponse;
import com.one.aim.rs.data.*;
import com.one.aim.service.*;
import com.one.exception.AppException;
import com.one.security.jwt.JwtUtils;
import com.one.service.impl.UserDetailsImpl;
import com.one.utils.PhoneUtils;
import com.one.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.CartBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.mapper.AdminMapper;
import com.one.aim.mapper.CartMapper;
import com.one.aim.mapper.SellerMapper;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.CartRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.rq.SellerRq;
import com.one.aim.rs.AdminRs;
import com.one.aim.rs.CartRs;
import com.one.aim.rs.SellerRs;
import com.one.constants.StringConstants;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepo sellerRepo;
    private final CartRepo cartRepo;
    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final EmailService emailService;
    private final UserRepo userRepo;
    private final AdminSettingService adminSettingService;
    private final NotificationService notificationService;

    // ===========================================================
    // SELLER SIGN-UP
    // ===========================================================
    @Override
    @Transactional
    public BaseRs saveSeller(SellerRq rq) throws Exception {

        // -----------------------------------------
        // CHECK: ADMIN DISABLED SELLER SIGNUP
        // -----------------------------------------
        if (!adminSettingService.getBooleanValue("feature_seller_applications", true)) {
            return ResponseUtils.failure("SELLER_SIGNUP_DISABLED",
                    "Seller registration is currently disabled by the admin.");
        }

        // -----------------------------------------
        // VALIDATION
        // -----------------------------------------
        List<String> errors = SellerHelper.validateSeller(rq, false);
        if (!errors.isEmpty()) {
            return ResponseUtils.failure("INVALID_INPUT", errors);
        }

        String email = rq.getEmail().trim().toLowerCase();
        String phone = PhoneUtils.normalize(rq.getPhoneNo());

        // -----------------------------------------
        // GLOBAL EMAIL UNIQUE CHECK
        // -----------------------------------------
        if (adminRepo.findByEmailIgnoreCase(email).isPresent()
                || userRepo.findByEmailIgnoreCase(email).isPresent()
                || sellerRepo.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseUtils.failure("EMAIL_EXISTS", "Email already registered.");
        }

        // -----------------------------------------
        // GLOBAL PHONE UNIQUE CHECK
        // -----------------------------------------
        boolean phoneExists =
                sellerRepo.existsByPhoneNo(phone) ||
                        adminRepo.existsByPhoneNo(phone) ||
                        userRepo.existsByPhoneNo(phone);

        if (phoneExists) {
            return ResponseUtils.failure("PHONE_EXISTS", "Phone number already registered.");
        }

        // -----------------------------------------
        // SELLER-ONLY UNIQUE CHECKS
        // -----------------------------------------

        // Aadhaar
        if (rq.getAdhaar() != null && sellerRepo.existsByAdhaar(rq.getAdhaar())) {
            return ResponseUtils.failure("AADHAAR_EXISTS", "Aadhaar number already used.");
        }

        // PAN
        if (rq.getPanCard() != null && sellerRepo.existsByPanCard(rq.getPanCard())) {
            return ResponseUtils.failure("PAN_EXISTS", "PAN card already used.");
        }

        // GST
        if (rq.getGst() != null && sellerRepo.existsByGst(rq.getGst())) {
            return ResponseUtils.failure("GST_EXISTS", "GST number already registered.");
        }

        // -----------------------------------------
        // CREATE SELLER OBJECT
        // -----------------------------------------
        SellerBO seller = new SellerBO();
        seller.setFullName(rq.getFullName());
        seller.setEmail(email);
        seller.setPhoneNo(phone);
        seller.setGst(rq.getGst());
        seller.setAdhaar(rq.getAdhaar());
        seller.setPanCard(rq.getPanCard());
        seller.setRole("SELLER");

        if (Utils.isNotEmpty(rq.getPassword())) {
            seller.setPassword(passwordEncoder.encode(rq.getPassword()));
        }

        // -----------------------------------------
        // IMAGE UPLOAD
        // -----------------------------------------
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            FileBO uploaded = fileService.uploadAndReturnFile(rq.getImage());
            seller.setImageFileId(uploaded.getId());
        }

        // -----------------------------------------
        // EMAIL VERIFICATION
        // -----------------------------------------
        String token = TokenUtils.generateVerificationToken();

        seller.setVerificationToken(token);
        seller.setVerificationTokenExpiry(TokenUtils.generateExpiry());
        seller.setEmailVerified(false);
        seller.setVerified(false);
        seller.setLocked(true);

        sellerRepo.save(seller);

        notificationService.notifyAdmins(
                "SELLER_REGISTERED",
                "New Seller Registered",
                seller.getFullName() + " (" + seller.getEmail() + ")",
                seller.getImageFileId(),
                seller.getId(),
                "/admin/sellers/" + seller.getId()
        );



        emailService.sendVerificationEmail(
                seller.getEmail(),
                seller.getFullName(),
                seller.getVerificationToken()
        );

        return ResponseUtils.success(
                new SellerDataRs(MessageCodes.MC_SAVED_SUCCESSFUL, SellerMapper.mapToSellerRs(seller))
        );
    }




    // ===========================================================
    // RETRIEVE LOGGED-IN SELLER
    // ===========================================================
    @Override
    public BaseRs retrieveSeller() {

        try {
            Long sellerId = AuthUtils.findLoggedInUser().getDocId();

            SellerBO seller = sellerRepo.findById(sellerId)
                    .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_SELLER_NOT_FOUND));

            SellerRs sellerRs = SellerMapper.mapToSellerRs(seller);

            return ResponseUtils.success(
                    new SellerDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, sellerRs)
            );

        } catch (Exception e) {
            log.error("retrieveSeller() error:", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }


    // ===========================================================
    // GET ALL SELLERS (ADMIN)
    // ===========================================================
    @Override
    public BaseRs retrieveSellers() {

        try {
            if (adminRepo.findById(AuthUtils.findLoggedInUser().getDocId()).isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_ADMIN_NOT_FOUND);
            }

            List<SellerBO> sellers = sellerRepo.findAll();

            List<SellerRs> sellerRsList = sellers.stream()
                    .map(seller -> {
                        SellerRs rs = SellerMapper.mapToSellerRs(seller);
                        rs.setDocId(null); // hide DB ID from admin
                        return rs;
                    })
                    .toList();

            return ResponseUtils.success(
                    new SellerDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, sellerRsList)
            );

        } catch (Exception e) {
            log.error("retrieveSellers() error:", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }



    // ===========================================================
    // GET SELLER CARTS
    // ===========================================================
    @Override
    public BaseRs retrieveSellerCarts() {

        validateSellerAccess();

        Long userPkId = AuthUtils.findLoggedInUser().getDocId();

        // Get the seller record to extract PUBLIC sellerId (String)
        SellerBO seller = sellerRepo.findById(userPkId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        String sellerId = seller.getSellerId();   // <-- String sellerId

        List<CartBO> carts = cartRepo.findAllBySellerId(sellerId);

        if (Utils.isEmpty(carts)) {
            return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_NO_RECORDS_FOUND));
        }

        return ResponseUtils.success(
                new CartDataRsList(
                        MessageCodes.MC_RETRIEVED_SUCCESSFUL,
                        CartMapper.mapToCartRsList(carts, fileService)
                )
        );
    }



    // ===========================================================
    // DELETE SELLER (ADMIN ONLY)
    // ===========================================================
    @Override
    @Transactional
    public BaseRs deleteSeller(String id) {

        Long adminId = AuthUtils.getLoggedUserId();

        if (adminRepo.findById(adminId).isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
        }

        //  use sellerId string, not numeric ID
        SellerBO seller = sellerRepo.findBySellerId(id)
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_SELLER_NOT_FOUND));

        if (!seller.isActive()) {
            return ResponseUtils.failure(
                    "SELLER_ALREADY_INACTIVE",
                    "Seller is already deactivated."
            );
        }

        seller.setActive(false);
        seller.setLocked(true);
        seller.setVerified(false);

        if (seller.getImageFileId() != null) {
            try {
                fileService.deleteFileById(String.valueOf(seller.getImageFileId()));
            } catch (Exception ignored) {}
            seller.setImageFileId(null);
        }

        sellerRepo.save(seller);

        return ResponseUtils.success(
                new SellerDataRs(
                        MessageCodes.MC_DELETED_SUCCESSFUL,
                        SellerMapper.mapToSellerRs(seller)
                )
        );
    }



//    // ===========================================================
//    // SEND VERIFICATION EMAIL
//    // ===========================================================
//    @Override
//    public void sendVerificationEmail(SellerBO seller) {
//
//        if (seller == null || seller.getEmail() == null) {
//            log.error("sendVerificationEmail() failed → Seller or Email missing");
//            return;
//        }
//
//        try {
//            String token = TokenUtils.generateVerificationToken();
//            seller.setVerificationToken(token);
//            seller.setVerificationTokenExpiry(TokenUtils.generateExpiry());
//            sellerRepo.save(seller);
//
//            emailService.sendVerificationEmail(
//                    seller.getEmail(),
//                    seller.getFullName(),
//                    token
//            );
//
//        } catch (Exception e) {
//            log.error("Failed to send verification email", e);
//        }
//    }


    // ===========================================================
    // UPDATE SELLER PROFILE
    // ===========================================================
    @Override
    @Transactional
    public BaseRs updateSellerProfile(String email, SellerRq rq) throws Exception {

        SellerBO seller = sellerRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));

        List<String> errors = SellerHelper.validateSeller(rq, true);
        if (!errors.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
        }

        boolean updated = false;

        // ---------------------------------------------------------
        // FULL NAME
        // ---------------------------------------------------------
        if (Utils.isNotEmpty(rq.getFullName())) {
            seller.setFullName(rq.getFullName());
            updated = true;
        }

        // ---------------------------------------------------------
        // PHONE NUMBER (normalize + uniqueness)
        // ---------------------------------------------------------
        if (Utils.isNotEmpty(rq.getPhoneNo())) {

            String normalized = PhoneUtils.normalize(rq.getPhoneNo());

            if (!normalized.equals(seller.getPhoneNo())) {

                boolean exists =
                        sellerRepo.existsByPhoneNo(normalized) ||
                                adminRepo.existsByPhoneNo(normalized) ||
                                userRepo.existsByPhoneNo(normalized);

                if (exists) {
                    return ResponseUtils.failure("PHONE_ALREADY_EXISTS", "Phone number already registered.");
                }

                seller.setPhoneNo(normalized);
                updated = true;
            }
        }

        // ---------------------------------------------------------
        // PASSWORD UPDATE
        // ---------------------------------------------------------
        if (Utils.isNotEmpty(rq.getPassword())) {
            seller.setPassword(passwordEncoder.encode(rq.getPassword()));
            updated = true;
        }

        // ---------------------------------------------------------
        // IMAGE UPDATE
        // ---------------------------------------------------------
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            FileBO uploaded = fileService.uploadAndReturnFile(rq.getImage());
            seller.setImageFileId(uploaded.getId());
            updated = true;
        }

        // ---------------------------------------------------------
        // EMAIL CHANGE (SHIFTED TO EmailService)
        // ---------------------------------------------------------
        if (Utils.isNotEmpty(rq.getEmail()) &&
                !rq.getEmail().equalsIgnoreCase(seller.getEmail())) {

            String newEmail = rq.getEmail().trim().toLowerCase();

            boolean emailExists =
                    sellerRepo.findByEmailIgnoreCase(newEmail).isPresent() ||
                            userRepo.findByEmailIgnoreCase(newEmail).isPresent() ||
                            adminRepo.findByEmailIgnoreCase(newEmail).isPresent();

            if (emailExists) {
                return ResponseUtils.failure("EMAIL_EXISTS", "Email already registered.");
            }

            //  CALL EMAIL SERVICE (moved the logic here)
            emailService.initiateSellerEmailChange(seller, newEmail);

            sellerRepo.save(seller);

            return ResponseUtils.success("Verification email sent. Please verify new email.");
        }

        // ---------------------------------------------------------
        // SAVE CHANGES (IF ANY)
        // ---------------------------------------------------------
        if (updated) {
            sellerRepo.save(seller);
            return ResponseUtils.success("Profile updated successfully.");
        }

        return ResponseUtils.failure("NO_CHANGES", "No valid fields provided.");
    }


//    // ===========================================================
//    // VERIFY EMAIL
//    // ===========================================================
//    @Override
//    @Transactional
//    public BaseRs verifyEmail(String token) {
//
//        SellerBO seller = sellerRepo.findByVerificationToken(token)
//                .orElse(null);
//
//        if (seller == null) {
//            return ResponseUtils.failure("INVALID_TOKEN");
//        }
//
//        if (seller.getVerificationTokenExpiry() == null ||
//                seller.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
//            return ResponseUtils.failure("TOKEN_EXPIRED");
//        }
//
//        // If seller was updating email
//        if (Utils.isNotEmpty(seller.getPendingEmail())) {
//            seller.setEmail(seller.getPendingEmail());
//            seller.setPendingEmail(null);
//        }
//
//        seller.setEmailVerified(true);
//
//        // IMPORTANT CHANGE (Allow login after email verification)
//        seller.setLocked(false);      // ✔ Seller can login
//        seller.setVerified(false);    // ✔ Still cannot add/edit/delete until admin approves
//
//        seller.setVerificationToken(null);
//        seller.setVerificationTokenExpiry(null);
//
//        sellerRepo.save(seller);
//
//        return ResponseUtils.success("Email verified successfully. You can login now, but admin approval is required to perform actions.");
//    }
//
//
//
//    // ===========================================================
//    // SEND ADMIN APPROVAL EMAIL
//    // ===========================================================
//    @Override
//    public void sendAdminApprovalEmail(SellerBO seller) {
//
//        if (seller == null || seller.getEmail() == null) {
//            return;
//        }
//
//        try {
//            emailService.sendSellerApprovalEmail(
//                    seller.getEmail(),
//                    seller.getFullName()
//            );
//
//        } catch (Exception e) {
//            log.error("Failed to send admin approval email", e);
//        }
//    }


    // ===========================================================
    // VALIDATE SELLER ACCESS (USED BY PRODUCT / CART)
    // ===========================================================
    private SellerBO validateSellerAccess() {

        Long id = AuthUtils.getLoggedUserId();
        String role = AuthUtils.getLoggedUserRole();

        if (!"SELLER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only sellers can access this resource.");
        }

        SellerBO seller = sellerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (!seller.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before performing this action.");
        }

        if (!seller.isVerified()) {
            throw new RuntimeException(
                    "Seller " + seller.getSellerId() + " → Your account is still pending admin approval."
            );
        }


        if (seller.isLocked()) {
            throw new RuntimeException("Your seller account is locked by admin.");
        }

        return seller;   // RETURN SAME AS BEFORE
    }

}

