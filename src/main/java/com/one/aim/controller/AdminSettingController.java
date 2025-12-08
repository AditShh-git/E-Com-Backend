package com.one.aim.controller;

import com.one.aim.rs.SellerRs;
import com.one.aim.rs.data.SellerDataRsList;
import com.one.aim.service.AdminService;
import com.one.aim.service.AdminSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@Slf4j
public class AdminSettingController {

    @Autowired
    private AdminSettingService adminSettingService;

    // ============================================================
// 1. Get ALL settings (GROUPED by UI sections)
// ============================================================
    @GetMapping("/all")
    public ResponseEntity<?> getAllSettings() throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing REST API [GET /api/admin/settings/all]");
        }

        Map<String, String> settings = adminSettingService.getAll();

        Map<String, Object> response = new LinkedHashMap<>();

        // === GROUPS BASED ON FIGMA UI ===
        response.put("general", Map.of(
                "platform_name", settings.get("platform_name"),
                "contact_email", settings.get("contact_email"),
                "default_language", settings.get("default_language"),
                "default_currency", settings.get("default_currency"),
                "time_zone", settings.get("time_zone")
        ));

        response.put("features", Map.of(
                "feature_reviews_enabled", settings.get("feature_reviews_enabled"),
                "feature_wishlist_enabled", settings.get("feature_wishlist_enabled"),
                "feature_seller_applications", settings.get("feature_seller_applications")
        ));

        response.put("payment", Map.of(
                "accepted_payment_methods", settings.get("accepted_payment_methods"),
                "payout_schedule_days", settings.get("payout_schedule_days"),
                "transaction_fee_percent", settings.get("transaction_fee_percent")
        ));

        // SHIPPING
        Map<String, String> shipping = new LinkedHashMap<>();
        shipping.put("default_shipping_provider", settings.get("default_shipping_provider"));
        shipping.put("default_shipping_rate", settings.get("default_shipping_rate"));
        shipping.put("delivery_regions", settings.get("delivery_regions"));
        shipping.put("default_tax_percent", settings.get("default_tax_percent"));
        shipping.put("delivery_charges_fixed", settings.get("delivery_charges_fixed"));
        shipping.put("tax_electronics", settings.get("tax_electronics"));
        shipping.put("shipping_electronics", settings.get("shipping_electronics"));
        shipping.put("tax_fashion", settings.get("tax_fashion"));
        shipping.put("shipping_fashion", settings.get("shipping_fashion"));
        shipping.put("tax_grocery", settings.get("tax_grocery"));
        shipping.put("shipping_grocery", settings.get("shipping_grocery"));

        response.put("shipping", shipping);


        response.put("policy", Map.of(
                "terms_url", settings.get("terms_url"),
                "privacy_url", settings.get("privacy_url"),
                "return_url", settings.get("return_url")
        ));

        response.put("notification", Map.of(
                "notify_new_order", settings.get("notify_new_order"),
                "notify_user_activity", settings.get("notify_user_activity")
        ));

        response.put("security", Map.of(
                "admin_user_roles", settings.get("admin_user_roles"),
                "session_timeout_minutes", settings.get("session_timeout_minutes")
        ));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ============================================================
    // 2. Get setting by key
    // ============================================================
    @GetMapping("/get")
    public ResponseEntity<?> getSetting(@RequestParam("key") String key) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing REST API [POST /api/admin/settings/get]");
        }

        return new ResponseEntity<>(adminSettingService.get(key), HttpStatus.OK);
    }

    // ============================================================
    // 3. Update/Create setting
    // ============================================================
    @PostMapping("/save")
    public ResponseEntity<?> saveSetting(
            @RequestParam("key") String key,
            @RequestParam("value") String value
    ) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing REST API [POST /api/admin/settings/save]");
        }

        return new ResponseEntity<>(adminSettingService.save(key, value), HttpStatus.OK);
    }

    // ==============================
// ADMIN → VERIFY SELLER
// ==============================
    @PostMapping("/seller/verify")
    public ResponseEntity<?> verifySeller(
            @RequestParam String sellerId,
            @RequestParam Boolean status) {

        return ResponseEntity.ok(adminSettingService.verifySeller(sellerId, status));
    }


    // ==============================
// LIST UNVERIFIED SELLERS
// ==============================
    @GetMapping("/seller/unverified")
    public ResponseEntity<?> getUnverifiedSellers() {

        List<SellerRs> list = adminSettingService.getUnverifiedSellers();

        return ResponseEntity.ok(
                new SellerDataRsList("UNVERIFIED_SELLERS", list)
        );
    }

    // ==============================
// LIST VERIFIED SELLERS
// ==============================
    @GetMapping("/seller/verified")
    public ResponseEntity<?> getVerifiedSellers() {

        List<SellerRs> list = adminSettingService.getVerifiedSellers();

        return ResponseEntity.ok(
                new SellerDataRsList("VERIFIED_SELLERS", list)
        );
    }

    @GetMapping("/seller/rejected")
    public ResponseEntity<?> getRejectedSellers() {

        List<SellerRs> list = adminSettingService.getRejectedSellers();

        return ResponseEntity.ok(
                new SellerDataRsList("REJECTED_SELLERS", list)
        );
    }

    @GetMapping("/{sellerId}/documents")
    public ResponseEntity<byte[]> downloadSellerDocuments(@PathVariable String sellerId) {

        byte[] zipBytes = adminSettingService.getSellerDocumentsZip(sellerId);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"seller-" + sellerId + "-documents.zip\"");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipBytes.length)
                .body(zipBytes);
    }

}
