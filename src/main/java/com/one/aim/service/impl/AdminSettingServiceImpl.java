package com.one.aim.service.impl;

import com.one.aim.bo.AdminSettingsBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.mapper.SellerMapper;
import com.one.aim.repo.AdminSettingsRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.rs.SellerRs;
import com.one.aim.service.AdminSettingService;
import com.one.aim.service.EmailService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminSettingServiceImpl implements AdminSettingService {

    private final AdminSettingsRepo repo;
    private final SellerRepo sellerRepo;
    private final EmailService  emailService;

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

        // Existing settings
        save("seller_verification_enabled", "true");
        save("default_tax_percent", "0");
        save("support_email", "support@aimdev.com");
        save("order_prefix", "AIM");
        save("allow_new_seller_registration", "true");

        save("return_policy_days", "7");               // default: 7-day return
        save("global_discount_percent", "0");          // admin default no discount
        save("enable_discount_engine", "false");       // discount system OFF initially
        save("low_stock_threshold", "5");              // notify when stock < 5
        save("delivery_charges_fixed", "50");          // ₹50 flat delivery charge

        log.info("Default admin settings initialized.");
    }


    // ===========================================
// VERIFY SELLER (ADMIN APPROVAL)
// ===========================================
    @Override
    public String verifySeller(Long sellerId, Boolean status) {

        SellerBO seller = sellerRepo.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // =============== ADMIN APPROVES SELLER ===============
        if (status) {

            // Only approve if not approved previously
            if (!seller.isVerified()) {

                seller.setVerified(true);   // Admin approved
                seller.setLocked(false);    // Ensure seller can login & perform actions

                // Send approval mail
                emailService.sendSellerApprovalEmail(
                        seller.getEmail(),
                        seller.getFullName()
                );

                sellerRepo.save(seller);
                return "Seller approved successfully. Approval email sent.";
            }

            return "Seller already verified.";
        }

        // =============== ADMIN REJECTS/UNVERIFIES SELLER ===============
        seller.setVerified(false);   // Admin removed approval
        seller.setLocked(false);     //  Keep unlocked so seller can still login

        // Seller CAN login → but cannot do any actions
        // Your validateSellerAccess() already blocks actions when verified = false

        sellerRepo.save(seller);
        return "Seller marked as unverified. Seller can login but cannot perform actions.";
    }



    @Override
    public List<SellerRs> getUnverifiedSellers() {
        return sellerRepo.findAll().stream()
                .filter(s -> !s.isVerified())
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


}
