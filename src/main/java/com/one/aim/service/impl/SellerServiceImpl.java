package com.one.aim.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.one.aim.bo.FileBO;
import com.one.aim.helper.SellerHelper;
import com.one.aim.repo.UserRepo;
import com.one.aim.rq.UpdateRq;
import com.one.aim.rs.data.*;
import com.one.aim.service.AdminSettingService;
import com.one.aim.service.EmailService;
import com.one.security.jwt.JwtUtils;
import com.one.service.impl.UserDetailsImpl;
import com.one.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.one.aim.service.FileService;
import com.one.aim.service.SellerService;
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

    // ===========================================================
    // SELLER SIGN-UP (ONLY SIGNUP) - Similar to User saveUser()
    // ===========================================================
    @Override
    @Transactional
    public BaseRs saveSeller(SellerRq rq) throws Exception {

        List<String> errors = SellerHelper.validateSeller(rq, false);
        if (!errors.isEmpty()) {
            return ResponseUtils.failure("INVALID_INPUT", errors);
        }

        String email = rq.getEmail().trim().toLowerCase();

        if (adminRepo.findByEmailIgnoreCase(email).isPresent()
                || userRepo.findByEmailIgnoreCase(email).isPresent()
                || sellerRepo.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseUtils.failure("EMAIL_EXISTS", "Email already registered with another account.");
        }


        SellerBO seller = new SellerBO();
        seller.setFullName(rq.getFullName());
        seller.setEmail(email);
        seller.setPhoneNo(rq.getPhoneNo());
        seller.setGst(rq.getGst());
        seller.setAdhaar(rq.getAdhaar());
        seller.setPanCard(rq.getPanCard());
        seller.setRole("SELLER");

        // password
        if (Utils.isNotEmpty(rq.getPassword())) {
            seller.setPassword(passwordEncoder.encode(rq.getPassword()));
        }

        // profile image
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            FileBO uploaded = fileService.uploadAndReturnFile(rq.getImage());
            seller.setImageFileId(uploaded.getId());
        }

        // email verification token
        String token = TokenUtils.generateVerificationToken();
        seller.setVerificationToken(token);
        seller.setVerificationTokenExpiry(TokenUtils.generateExpiry());
        seller.setEmailVerified(false);
        seller.setVerified(false);
        seller.setLocked(true);

        sellerRepo.save(seller);

        // send email verification
        emailService.sendVerificationEmail(
                seller.getEmail(),
                seller.getFullName(),
                token
        );

        return ResponseUtils.success(
                new SellerDataRs(MessageCodes.MC_SAVED_SUCCESSFUL, SellerMapper.mapToSellerRs(seller))
        );
    }

    // ===========================================================
    // RETRIEVE SINGLE SELLER (similar to retrieveUser)
    // ===========================================================
    @Override
    public BaseRs retrieveSeller() {
        try {
            Long sellerId = AuthUtils.findLoggedInUser().getDocId();

            SellerBO seller = sellerRepo.findById(sellerId)
                    .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_SELLER_NOT_FOUND));

            SellerRs sellerRs = SellerMapper.mapToSellerRs(seller);

            if (seller.getImageFileId() != null) {
                try {
                    sellerRs.setImage(fileService.getContentFromGridFS(String.valueOf(seller.getImageFileId())));
                } catch (Exception e) {
                    log.warn("Image retrieval failed for seller {}", sellerId);
                }
            }

            return ResponseUtils.success(
                    new SellerDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, sellerRs)
            );

        } catch (Exception e) {
            log.error("retrieveSeller() error:", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }

    // ===========================================================
    // RETRIEVE ALL SELLERS (Admin only)
    // ===========================================================
    @Override
    public BaseRs retrieveSellers() {

        try {
            if (adminRepo.findById(AuthUtils.findLoggedInUser().getDocId()).isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_ADMIN_NOT_FOUND);
            }

            List<SellerBO> sellers = sellerRepo.findAll();
            List<SellerRs> sellerRsList = SellerMapper.mapToSellerRsList(sellers);

            return ResponseUtils.success(
                    new SellerDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, sellerRsList)
            );

        } catch (Exception e) {
            log.error("retrieveSellers() error:", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }

    // ===========================================================
    // RETRIEVE SELLER CARTS
    // ===========================================================
    @Override
    public BaseRs retrieveSellerCarts() {

        Long sellerId = AuthUtils.findLoggedInUser().getDocId();
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
    // DELETE SELLER (Admin only)
    // ===========================================================
    @Override
    @Transactional
    public BaseRs deleteSeller(String id) {

        try {
            if (adminRepo.findById(AuthUtils.findLoggedInUser().getDocId()).isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
            }

            SellerBO seller = sellerRepo.findById(Long.valueOf(id))
                    .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_SELLER_NOT_FOUND));

            if (seller.getImageFileId() != null) {
                try {
                    fileService.deleteFileById(String.valueOf(seller.getImageFileId()));
                } catch (Exception ignored) {}
            }

            sellerRepo.delete(seller);

            return ResponseUtils.success(
                    new SellerDataRs(MessageCodes.MC_DELETED_SUCCESSFUL, SellerMapper.mapToSellerRs(seller))
            );

        } catch (Exception e) {
            log.error("deleteSeller() error:", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }

    @Override
    public void sendVerificationEmail(SellerBO seller) {

        if (seller == null || seller.getEmail() == null) {
            log.error("sendVerificationEmail() failed → Seller or Email missing");
            return;
        }

        try {
            // Generate token + expiry
            String token = TokenUtils.generateVerificationToken();
            seller.setVerificationToken(token);
            seller.setVerificationTokenExpiry(TokenUtils.generateExpiry());

            sellerRepo.save(seller);

            // Send email
            emailService.sendVerificationEmail(
                    seller.getEmail(),
                    seller.getFullName(),
                    token
            );

            log.info("Verification email sent to seller: {}", seller.getEmail());

        } catch (Exception e) {
            log.error("Failed to send verification email to seller {}", seller.getEmail(), e);
        }
    }


    // ===========================================================
    // UPDATE SELLER PROFILE (same UX as user update)
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

        if (Utils.isNotEmpty(rq.getFullName())) {
            seller.setFullName(rq.getFullName());
            updated = true;
        }

        if (Utils.isNotEmpty(rq.getPhoneNo())) {
            seller.setPhoneNo(rq.getPhoneNo());
            updated = true;
        }

        if (Utils.isNotEmpty(rq.getPassword())) {
            seller.setPassword(passwordEncoder.encode(rq.getPassword()));
            updated = true;
        }

        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            FileBO uploaded = fileService.uploadAndReturnFile(rq.getImage());
            seller.setImageFileId(uploaded.getId());
            updated = true;
        }

        // EMAIL CHANGE → Needs verification
        if (Utils.isNotEmpty(rq.getEmail())
                && !rq.getEmail().equalsIgnoreCase(seller.getEmail())) {

            String newEmail = rq.getEmail().trim().toLowerCase();

            if (sellerRepo.findByEmailIgnoreCase(newEmail).isPresent()) {
                return ResponseUtils.failure("EMAIL_EXISTS", "Email already exists.");
            }

            seller.setPendingEmail(newEmail);
            seller.setVerificationToken(TokenUtils.generateVerificationToken());
            seller.setVerificationTokenExpiry(TokenUtils.generateExpiry());
            seller.setEmailVerified(false);
            seller.setLocked(true);

            sellerRepo.save(seller);

            emailService.sendVerificationEmail(
                    newEmail,
                    seller.getFullName(),
                    seller.getVerificationToken()
            );

            return ResponseUtils.success("Verification email sent. Please verify to update email.");
        }

        if (updated) {
            sellerRepo.save(seller);
            return ResponseUtils.success("Profile updated successfully.");
        }

        return ResponseUtils.failure("NO_CHANGES", "No valid fields provided.");
    }

    // ===========================================================
    // VERIFY EMAIL (for signup + email update)
    // ===========================================================
    @Override
    @Transactional
    public BaseRs verifyEmail(String token) {

        SellerBO seller = sellerRepo.findByVerificationToken(token)
                .orElse(null);

        if (seller == null) {
            return ResponseUtils.failure("INVALID_TOKEN");
        }

        if (seller.getVerificationTokenExpiry() == null ||
                seller.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseUtils.failure("TOKEN_EXPIRED");
        }

        // If email update
        if (Utils.isNotEmpty(seller.getPendingEmail())
                && !seller.getPendingEmail().equalsIgnoreCase(seller.getEmail())) {
            seller.setEmail(seller.getPendingEmail());
            seller.setPendingEmail(null);
        }

        seller.setEmailVerified(true);
        seller.setLocked(true);  // Admin approval still required
        seller.setVerified(false);
        seller.setVerificationToken(null);
        seller.setVerificationTokenExpiry(null);

        sellerRepo.save(seller);

        return ResponseUtils.success("Email verified successfully.");
    }

    @Override
    public void sendAdminApprovalEmail(SellerBO seller) {

        if (seller == null || seller.getEmail() == null) {
            log.error("sendAdminApprovalEmail() failed → Seller or Email missing");
            return;
        }

        try {
            emailService.sendSellerApprovalEmail(
                    seller.getEmail(),
                    seller.getFullName()
            );

            log.info("Admin approval email sent to seller {}", seller.getEmail());

        } catch (Exception e) {
            log.error("Failed to send admin approval email to seller {}", seller.getEmail(), e);
        }
    }

}
