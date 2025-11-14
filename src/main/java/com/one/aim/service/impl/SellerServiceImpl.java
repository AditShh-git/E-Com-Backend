package com.one.aim.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.one.aim.bo.FileBO;
import com.one.aim.helper.SellerHelper;
import com.one.aim.rq.UpdateRq;
import com.one.aim.rs.data.*;
import com.one.aim.service.AdminSettingService;
import com.one.aim.service.EmailService;
import com.one.security.jwt.JwtUtils;
import com.one.service.impl.UserDetailsImpl;
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
    private final JwtUtils jwtUtils;
    private final AdminSettingService  adminSettingService;

    // ===========================================================
    // Sign Up / Update Seller
    // ===========================================================
    @Override
    public BaseRs saveSeller(SellerRq rq) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing saveSeller(SellerRq) ->");
        }

        String docId = Utils.getValidString(rq.getDocId());
        String message = StringConstants.EMPTY;
        SellerBO sellerBO;

        // ===========================================================
        //  If updating existing seller (admin verifying)
        // ===========================================================
        if (Utils.isNotEmpty(docId)) {
            long id = Long.parseLong(docId);
            Optional<SellerBO> optSeller = sellerRepo.findById(id);
            if (optSeller.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_SELLER_NOT_FOUND);
            }

            sellerBO = optSeller.get();

            AdminBO adminBO = adminRepo.findByIdAndFullName(
                    AuthUtils.findLoggedInUser().getDocId(),
                    AuthUtils.findLoggedInUser().getFullName());

            if (adminBO == null && Boolean.parseBoolean(rq.getIsVerified())) {
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
            }

            sellerBO.setVerified(Boolean.parseBoolean(rq.getIsVerified()));

            //  Validate only updatable fields (isUpdate = true)
            List<String> validationErrors = SellerHelper.validateSeller(rq, true);
            if (!validationErrors.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, validationErrors);
            }

            message = MessageCodes.MC_UPDATED_SUCCESSFUL;
        }
        // ===========================================================
        //  Else, creating new seller (signup)
        // ===========================================================
        else {

                // Check system setting: allow or block new seller registration
                String allow = adminSettingService.get("allow_new_seller_registration");

                if ("false".equalsIgnoreCase(allow)) {
                    return ResponseUtils.failure("SELLER_REGISTRATION_DISABLED",
                            "Seller registration is currently disabled by admin.");
                }

                sellerBO = new SellerBO();

                // Full validation required
                List<String> validationErrors = SellerHelper.validateSeller(rq, false);
                if (!validationErrors.isEmpty()) {
                    return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, validationErrors);
                }

                message = MessageCodes.MC_SAVED_SUCCESSFUL;
        }

        // ===========================================================
        //  Map request fields
        // ===========================================================
        sellerBO.setFullName(Utils.getValidString(rq.getFullName()));
        sellerBO.setEmail(Utils.getValidString(rq.getEmail()));
        sellerBO.setPhoneNo(Utils.getValidString(rq.getPhoneNo()));
        sellerBO.setGst(Utils.getValidString(rq.getGst()));
        sellerBO.setAdhaar(Utils.getValidString(rq.getAdhaar()));
        sellerBO.setPanCard(Utils.getValidString(rq.getPanCard()));

        String rawPassword = Utils.getValidString(rq.getPassword());
        if (Utils.isNotEmpty(rawPassword)) {
            if (sellerBO.getPassword() == null || !passwordEncoder.matches(rawPassword, sellerBO.getPassword())) {
                sellerBO.setPassword(passwordEncoder.encode(rawPassword));
            }
        }

        sellerBO.setRole("SELLER");

        // Handle image upload
        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            FileBO uploadedFile = fileService.uploadAndReturnFile(rq.getImage());
            sellerBO.setImageFileId(uploadedFile.getId());
        }


        sellerRepo.save(sellerBO);
        SellerRs sellerRs = SellerMapper.mapToSellerRs(sellerBO);

        return ResponseUtils.success(new SellerDataRs(message, sellerRs));
    }


    @Override
    public BaseRs signInSeller(String email, String password) throws Exception {
        log.debug("Executing signInSeller(email) -> {}", email);

        Optional<SellerBO> optSeller = sellerRepo.findByEmail(email);
        if (optSeller.isEmpty()) {
            log.error(ErrorCodes.EC_SELLER_NOT_FOUND);
            return ResponseUtils.failure(ErrorCodes.EC_SELLER_NOT_FOUND);
        }

        SellerBO seller = optSeller.get();

        if (!passwordEncoder.matches(password, seller.getPassword())) {
            log.error(ErrorCodes.EC_INVALID_CREDENTIALS);
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_CREDENTIALS);
        }

        if (!seller.isVerified()) {
            log.warn("Seller [{}] attempted login but not verified", seller.getEmail());
            return ResponseUtils.failure("Account not verified. Please contact admin.");
        }

        seller.setLogin(true);
        sellerRepo.save(seller);

        //  Generate tokens using EMAIL
        String accessToken = jwtUtils.generateAccessToken(seller.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(seller.getEmail());

        SellerRs sellerRs = SellerMapper.mapToSellerRs(seller);
        String message = MessageCodes.MC_LOGIN_SUCCESSFUL;

        //  Add tokens to response
        LoginDataRs loginData = new LoginDataRs(message, accessToken, refreshToken,
                seller.getId(), seller.getEmail(), seller.getFullName(), seller.getEmail());

        return ResponseUtils.success(loginData);
    }




    // ===========================================================
    // Retrieve Single Seller
    // ===========================================================
    @Override
    public BaseRs retrieveSeller() throws Exception {
        log.debug("Executing retrieveSeller() ->");

        try {
            //  Get logged-in seller
            Long sellerId = AuthUtils.findLoggedInUser().getDocId();
            Optional<SellerBO> optSeller = sellerRepo.findById(sellerId);

            if (optSeller.isEmpty()) {
                log.error(ErrorCodes.EC_SELLER_NOT_FOUND);
                return ResponseUtils.failure(ErrorCodes.EC_SELLER_NOT_FOUND);
            }

            SellerBO sellerBO = optSeller.get();

            //  Map entity → response DTO
            SellerRs sellerRs = SellerMapper.mapToSellerRs(sellerBO);

            //  Retrieve and attach image bytes (if using FileService storage)
            if (sellerBO.getImageFileId() != null) {
                try {
                    byte[] fileBytes = fileService.getContentFromGridFS(String.valueOf(sellerBO.getImageFileId()));
                    if (fileBytes != null && fileBytes.length > 0) {
                        sellerRs.setImage(fileBytes); // frontend can Base64 encode
                    } else {
                        log.warn("No image found for Seller ID: {}", sellerBO.getId());
                    }
                } catch (Exception ex) {
                    log.error("Error fetching image for Seller ID: {} -> {}", sellerBO.getId(), ex.getMessage());
                    // Don't block response just because image retrieval failed
                }
            }

            String message = MessageCodes.MC_RETRIEVED_SUCCESSFUL;
            return ResponseUtils.success(new SellerDataRs(message, sellerRs));

        } catch (Exception e) {
            log.error("Exception in retrieveSeller() -> ", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }



    // ===========================================================
    // Retrieve All Sellers (Admin only)
    // ===========================================================
    @Override
    public BaseRs retrieveSellers() throws Exception {
        log.debug("Executing retrieveSellers() ->");

        try {
            Optional<AdminBO> optAdmin = adminRepo.findById(AuthUtils.findLoggedInUser().getDocId());
            if (optAdmin.isEmpty()) {
                log.error(ErrorCodes.EC_ADMIN_NOT_FOUND);
                return ResponseUtils.failure(ErrorCodes.EC_ADMIN_NOT_FOUND);
            }

            List<SellerBO> sellers = sellerRepo.findAll();
            List<SellerRs> sellerRsList = SellerMapper.mapToSellerRsList(sellers);
            return ResponseUtils.success(new SellerDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, sellerRsList));
        } catch (Exception e) {
            log.error("Exception retrieveSellers() -> ", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }

    // ===========================================================
    // Retrieve Seller's Carts
    // ===========================================================
    @Override
    public BaseRs retrieveSellerCarts() throws Exception {
        log.debug("Executing retrieveSellerCarts() ->");

        List<CartBO> carts = cartRepo.findAllByCartempid(AuthUtils.findLoggedInUser().getDocId());
        if (Utils.isEmpty(carts)) {
            return ResponseUtils.success(new SellerDataRs(MessageCodes.MC_NO_RECORDS_FOUND));
        }

        List<CartRs> cartRsList = CartMapper.mapToCartRsList(carts);
        return ResponseUtils.success(new CartDataRsList(MessageCodes.MC_RETRIEVED_SUCCESSFUL, cartRsList));
    }

    // ===========================================================
// Delete Seller (Admin Only)
// ===========================================================
    @Override
    public BaseRs deleteSeller(String id) throws Exception {
        log.debug("Executing deleteSeller(id) ->");

        try {
            // ===========================================================
            //  Validate Admin Access
            // ===========================================================
            Optional<AdminBO> optAdmin = adminRepo.findById(AuthUtils.findLoggedInUser().getDocId());
            if (optAdmin.isEmpty()) {
                log.error(ErrorCodes.EC_ACCESS_DENIED);
                return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
            }

            // ===========================================================
            //  Validate Seller Exists
            // ===========================================================
            Optional<SellerBO> optSeller = sellerRepo.findById(Long.valueOf(id));
            if (optSeller.isEmpty()) {
                log.error(ErrorCodes.EC_SELLER_NOT_FOUND);
                return ResponseUtils.failure(ErrorCodes.EC_SELLER_NOT_FOUND);
            }

            SellerBO sellerBO = optSeller.get();

            // ===========================================================
            //  Delete Seller’s Uploaded Image (if exists)
            // ===========================================================
            if (sellerBO.getImageFileId() != null) {
                try {
                    fileService.deleteFileById(String.valueOf(sellerBO.getImageFileId()));
                    log.info("Deleted image file [ID={}] for seller [{}]",
                            sellerBO.getImageFileId(), sellerBO.getFullName());
                } catch (Exception e) {
                    log.warn("Failed to delete image file for seller [{}]: {}",
                            sellerBO.getFullName(), e.getMessage());
                    // continue even if file delete fails
                }
            }

            // ===========================================================
            //  Delete Seller Record
            // ===========================================================
            sellerRepo.deleteById(sellerBO.getId());

            log.info("Admin [{}] deleted seller [{}]",
                    AuthUtils.findLoggedInUser().getFullName(),
                    sellerBO.getFullName());

            // ===========================================================
            //  Prepare Response
            // ===========================================================
            SellerRs sellerRs = SellerMapper.mapToSellerRs(sellerBO);
            return ResponseUtils.success(new SellerDataRs(MessageCodes.MC_DELETED_SUCCESSFUL, sellerRs));

        } catch (NumberFormatException e) {
            log.error("Invalid seller ID format: {}", id);
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT);

        } catch (Exception e) {
            log.error("Exception deleteSeller(id) -> ", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }


    // ===========================================================
// Forgot Password (Seller)
// ===========================================================
    @Override
    public BaseRs forgotPassword(String email) throws Exception {
        log.debug("Executing forgotPassword(email) -> {}", email);
        log.info(" Normalized email before lookup: '{}'", email.trim().toLowerCase());


        String normalizedEmail = email.trim().toLowerCase();
        Optional<SellerBO> optSeller = sellerRepo.findByEmailIgnoreCase(normalizedEmail);

        if (optSeller.isEmpty()) {
            log.error(ErrorCodes.EC_SELLER_NOT_FOUND);
            return ResponseUtils.failure(ErrorCodes.EC_SELLER_NOT_FOUND);
        }

        SellerBO seller = optSeller.get();

        // Generate new token
        String token = UUID.randomUUID().toString();
        seller.setResetToken(token);
        seller.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30)); // valid for 30 minutes
        sellerRepo.save(seller);

        // Send reset email
        String resetLink = "https://yourfrontend.com/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String body = "Hello " + seller.getFullName() + ",\n\n" +
                "Please click the link below to reset your password:\n" +
                resetLink + "\n\n" +
                "This link will expire in 30 minutes.\n\n" +
                "If you didn’t request this, please ignore this email.";

        try {
            emailService.sendVerificationEmail(seller.getEmail(), subject, body); //  Reuse your email sender
        } catch (Exception e) {
            log.error("Error sending password reset email: {}", e.getMessage());
            return ResponseUtils.failure(ErrorCodes.EC_EMAIL_SEND_FAILED);
        }

        return ResponseUtils.success("Password reset link sent successfully to your email.");
    }

    // ===========================================================
// Reset Password (Seller)
// ===========================================================
    @Override
    public BaseRs resetPassword(String token, String newPassword) throws Exception {
        log.debug("Executing resetPassword(token) ->");

        Optional<SellerBO> optSeller = sellerRepo.findAll().stream()
                .filter(s -> token.equals(s.getResetToken()))
                .findFirst();

        if (optSeller.isEmpty()) {
            log.error(ErrorCodes.EC_INVALID_TOKEN);
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_TOKEN);
        }

        SellerBO seller = optSeller.get();

        if (seller.getResetTokenExpiry() == null || seller.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            log.error(ErrorCodes.EC_TOKEN_EXPIRED);
            return ResponseUtils.failure(ErrorCodes.EC_TOKEN_EXPIRED);
        }

        // Encode and set new password
        seller.setPassword(passwordEncoder.encode(newPassword));

        // Invalidate token
        seller.setResetToken(null);
        seller.setResetTokenExpiry(null);
        sellerRepo.save(seller);

        return ResponseUtils.success("Password reset successful. You can now log in with your new password.");
    }

    // ===========================================================
// UPDATE SELLER PROFILE
// ===========================================================
    @Override
    @Transactional
    public BaseRs updateSellerProfile(String email, SellerRq rq) throws Exception {

        SellerBO seller = sellerRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found with email: " + email));

        //  Validate input (isUpdate = true)
        List<String> validationErrors = SellerHelper.validateSeller(rq, true);
        if (!validationErrors.isEmpty()) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, validationErrors);
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

        //  Optional password change
        if (Utils.isNotEmpty(rq.getPassword()) && rq.getPassword().length() >= 6) {
            seller.setPassword(passwordEncoder.encode(rq.getPassword()));
            updated = true;
        }

        if (rq.getImage() != null && !rq.getImage().isEmpty()) {
            FileBO uploadedFile = fileService.uploadAndReturnFile(rq.getImage());
            seller.setImageFileId(uploadedFile.getId());
            updated = true;
        }


        if (updated) {
            sellerRepo.save(seller);
            return ResponseUtils.success(new SellerDataRs(MessageCodes.MC_UPDATED_SUCCESSFUL,
                    SellerMapper.mapToSellerRs(seller)));
        }

        return ResponseUtils.failure("NO_CHANGES", "No valid fields provided to update.");
    }


}