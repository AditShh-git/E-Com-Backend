package com.one.aim.controller;

import com.one.aim.service.AdminService;
import com.one.aim.service.AdminSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@Slf4j
public class AdminSettingController {

    @Autowired
    private AdminSettingService adminSettingService;

    // ============================================================
    // 1. Get ALL settings
    // ============================================================
    @GetMapping("/all")
    public ResponseEntity<?> getAllSettings() throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing REST API [POST /api/admin/settings/all]");
        }

        return new ResponseEntity<>(adminSettingService.getAll(), HttpStatus.OK);
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
            @RequestParam Long sellerId,
            @RequestParam Boolean status) {

        log.info("Admin verifying seller {} => {}", sellerId, status);

        return ResponseEntity.ok(adminSettingService.verifySeller(sellerId, status));
    }

    // ==============================
// LIST UNVERIFIED SELLERS
// ==============================
    @GetMapping("/seller/unverified")
    public ResponseEntity<?> getUnverifiedSellers() {
        return ResponseEntity.ok(adminSettingService.getUnverifiedSellers());
    }

    // ==============================
// LIST VERIFIED SELLERS
// ==============================
    @GetMapping("/seller/verified")
    public ResponseEntity<?> getVerifiedSellers() {
        return ResponseEntity.ok(adminSettingService.getVerifiedSellers());
    }

}
