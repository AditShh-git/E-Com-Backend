package com.one.aim.controller;

import com.one.aim.rq.LoginRq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.rq.SellerRq;
import com.one.aim.service.SellerService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@Slf4j
public class SellerController {

    private final SellerService sellerService;

    // ===========================================================
    // SELLER SIGN-UP (multipart/form-data)
    // ===========================================================
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveSeller(@ModelAttribute SellerRq rq) throws Exception {
        log.debug("Executing [POST /api/seller/signup]");
        return new ResponseEntity<>(sellerService.saveSeller(rq), HttpStatus.OK);
    }

    // ===========================================================
    // SELLER SIGN-IN
    // ===========================================================
    @PostMapping("/signin")
    public ResponseEntity<?> signInSeller(@RequestBody LoginRq rq) throws Exception {
        log.debug("Executing [POST /api/seller/signin]");
        return new ResponseEntity<>(sellerService.signInSeller(rq.getEmail(), rq.getPassword()), HttpStatus.OK);
    }

    // ===========================================================
    // GET LOGGED-IN SELLER
    // ===========================================================
    @GetMapping("/me")
    public ResponseEntity<?> retrieveSeller() throws Exception {
        log.debug("Executing [GET /api/seller/me]");
        return new ResponseEntity<>(sellerService.retrieveSeller(), HttpStatus.OK);
    }

    // ===========================================================
    // GET ALL SELLERS (Admin)
    // ===========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> retrieveSellers() throws Exception {
        log.debug("Executing [GET /api/seller/all]");
        return new ResponseEntity<>(sellerService.retrieveSellers(), HttpStatus.OK);
    }

    // ===========================================================
    // GET SELLER CARTS
    // ===========================================================
    @GetMapping("/carts")   // ✅ FINAL PATH = /api/seller/carts (No conflict)
    public ResponseEntity<?> retrieveSellerCarts() throws Exception {
        log.debug("Executing [GET /api/seller/carts]");
        return new ResponseEntity<>(sellerService.retrieveSellerCarts(), HttpStatus.OK);
    }

    // ===========================================================
    // DELETE SELLER (Admin)
    // ===========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSeller(@PathVariable String id) throws Exception {
        log.debug("Executing [DELETE /api/seller/{}]", id);
        return new ResponseEntity<>(sellerService.deleteSeller(id), HttpStatus.OK);
    }

    // ===========================================================
    // FORGOT PASSWORD
    // ===========================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) throws Exception {
        log.debug("Executing [POST /api/seller/forgot-password]");
        return new ResponseEntity<>(sellerService.forgotPassword(email), HttpStatus.OK);
    }

    // ===========================================================
    // RESET PASSWORD
    // ===========================================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) throws Exception {

        log.debug("Executing [POST /api/seller/reset-password]");
        return new ResponseEntity<>(sellerService.resetPassword(token, newPassword), HttpStatus.OK);
    }
}