package com.one.aim.service.impl;

import com.one.aim.bo.AdminSettingsBO;
import com.one.aim.bo.FileBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.mapper.SellerMapper;
import com.one.aim.repo.AdminSettingsRepo;
import com.one.aim.repo.FileRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.rs.SellerRs;
import com.one.aim.service.AdminSettingService;
import com.one.aim.service.EmailService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminSettingServiceImpl implements AdminSettingService {

    private final AdminSettingsRepo repo;
    private final SellerRepo sellerRepo;
    private final EmailService emailService;
    private final FileRepo fileRepo;

    @PostConstruct
    public void init() {
        initDefaultSettings();
    }

    @Override
    public String get(String key) {
        log.info("Fetching admin setting for key: {}", key);
        return repo.findByKey(key)
                .map(AdminSettingsBO::getValue)
                .orElse(null);
    }

    @Override
    public AdminSettingsBO save(String key, String value) {
        log.info("Saving admin setting: {} = {}", key, value);

        AdminSettingsBO s = repo.findByKey(key)
                .orElse(AdminSettingsBO.builder().key(key).build());

        s.setValue(value);
        return repo.save(s);
    }

    @Override
    public Map<String, String> getAll() {
        log.info("Fetching all admin settings");
        return repo.findAll().stream()
                .collect(Collectors.toMap(
                        AdminSettingsBO::getKey,
                        AdminSettingsBO::getValue
                ));
    }

    @Override
    public void initDefaultSettings() {
        log.info("Initializing default admin settings...");

        // GENERAL SETTINGS
        save("platform_name", "OneAim Store");
        save("contact_email", "support@aimdev.com");
        save("default_language", "en");
        save("default_currency", "INR");
        save("time_zone", "Asia/Kolkata");

        // FEATURE CONFIGURATION (UI Toggles)
        save("feature_reviews_enabled", "true");
        save("feature_wishlist_enabled", "true");
        save("feature_seller_applications", "true");

        // PAYMENT SETTINGS
        save("accepted_payment_methods", "COD,UPI,CARD");
        save("payout_schedule_days", "7");
        save("transaction_fee_percent", "0");

        // SHIPPING SETTINGS (NEW + EXISTING)
        save("default_shipping_provider", "INDIA_POST");
        save("default_shipping_rate", "50");
        save("delivery_regions", "INDIA");

        // CATEGORY BASED CHARGES (KEEPING EXISTING LOGIC)
        save("default_tax_percent", "0");
        save("delivery_charges_fixed", "50");
        save("tax_electronics", "18");
        save("shipping_electronics", "100");
        save("tax_fashion", "5");
        save("shipping_fashion", "50");
        save("tax_grocery", "0");
        save("shipping_grocery", "20");

        // POLICY MANAGEMENT
        save("terms_url", "");
        save("privacy_url", "");
        save("return_url", "");

        // NOTIFICATION SETTINGS
        save("notify_new_order", "true");
        save("notify_user_activity", "false");

        // SECURITY SETTINGS
        save("admin_user_roles", "ADMIN,MANAGER");
        save("session_timeout_minutes", "30");

        // DISCOUNT ENGINE (KEEP)
        save("global_discount_percent", "0");
        save("enable_discount_engine", "false");

        log.info("Default admin settings initialized.");
    }

    @Override
    public String verifySeller(String idOrCode, Boolean status) {
        SellerBO seller;

        if (idOrCode.matches("\\d+")) {
            seller = sellerRepo.findById(Long.parseLong(idOrCode))
                    .orElseThrow(() -> new RuntimeException("Seller not found"));
        } else {
            seller = sellerRepo.findBySellerId(idOrCode)
                    .orElseThrow(() -> new RuntimeException("Seller not found"));
        }

        if (Boolean.TRUE.equals(status)) {
            if (!seller.isVerified() || seller.isRejected()) {
                seller.setVerified(true);
                seller.setRejected(false);
                seller.setLocked(false);

                emailService.sendSellerApprovalEmail(
                        seller.getEmail(),
                        seller.getFullName()
                );

                sellerRepo.save(seller);
                return "Seller approved successfully. Email sent.";
            }
            return "Seller already verified.";
        }

        seller.setVerified(false);
        seller.setRejected(true);
        seller.setLocked(true);

        sellerRepo.save(seller);

        try {
            emailService.sendSellerRejectionEmail(
                    seller.getEmail(),
                    seller.getFullName()
            );
        } catch (Exception e) {
            log.warn("Failed to send rejection email to {} : {}", seller.getEmail(), e.getMessage());
        }

        return "Seller rejected successfully. Email sent.";
    }

    @Override
    public List<SellerRs> getUnverifiedSellers() {
        return sellerRepo.findAll().stream()
                .filter(s -> !s.isVerified() && !s.isRejected())
                .map(SellerMapper::mapToSellerRs)
                .collect(Collectors.toList());
    }

    @Override
    public List<SellerRs> getVerifiedSellers() {
        return sellerRepo.findAll().stream()
                .filter(SellerBO::isVerified)
                .map(SellerMapper::mapToSellerRs)
                .collect(Collectors.toList());
    }

    @Override
    public List<SellerRs> getRejectedSellers() {
        return sellerRepo.findAll().stream()
                .filter(SellerBO::isRejected)
                .map(SellerMapper::mapToSellerRs)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] getSellerDocumentsZip(String sellerId) {
        SellerBO seller = sellerRepo.findBySellerId(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        List<Long> fileIds = new ArrayList<>();

        if (seller.getImageFileId() != null)
            fileIds.add(seller.getImageFileId());

        if (fileIds.isEmpty()) {
            throw new RuntimeException("No documents found for seller.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Long fileId : fileIds) {

                FileBO fileBo = fileRepo.findById(fileId).orElse(null);
                if (fileBo == null) continue;

                zos.putNextEntry(new ZipEntry(fileBo.getName()));
                zos.write(fileBo.getInputstream());
                zos.closeEntry();
            }

        } catch (Exception e) {
            log.error("Error preparing ZIP: {}", e.getMessage());
            throw new RuntimeException("Failed to prepare seller documents", e);
        }

        return baos.toByteArray();
    }

    @Override
    public int getGlobalDiscount() {
        String value = get("global_discount_percent");
        try {
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean isDiscountEngineEnabled() {
        String value = get("enable_discount_engine");
        return "true".equalsIgnoreCase(value);
    }

    @Override
    public double getDefaultTaxPercent() {
        try {
            return Double.parseDouble(get("default_tax_percent"));
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public double getDeliveryChargeDefault() {
        try {
            return Double.parseDouble(get("delivery_charges_fixed"));
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public int getDefaultReturnPolicyDays() {
        try {
            return Integer.parseInt(get("return_policy_days"));
        } catch (Exception e) {
            return 7; // safe fallback
        }
    }

    @Override
    public double getDoubleValue(String key, double defaultValue) {
        try {
            String val = get(key);
            return val != null ? Double.parseDouble(val) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public long getLongValue(String key, long defaultValue) {
        try {
            String val = get(key);
            return val != null ? Long.parseLong(val) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBooleanValue(String key, boolean defaultValue) {
        try {
            String val = get(key);
            return val != null ? Boolean.parseBoolean(val) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }



}
