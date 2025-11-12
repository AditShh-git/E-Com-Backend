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
@RequestMapping(value = "/api")
@Slf4j
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    // ===========================================================
    // Seller Sign Up (with multipart/form-data)
    // ===========================================================
    @PostMapping(value = "/auth/signup/seller", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveSeller(@ModelAttribute @Valid SellerRq rq) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Executing RESTfulService [POST /auth/signup/seller]");
        }

        // image is already in rq.getImage()
        return new ResponseEntity<>(sellerService.saveSeller(rq), HttpStatus.OK);
    }

    // ===========================================================
// Seller Sign-In (Login)
// ===========================================================
    @PostMapping("/auth/signin/seller")
    public ResponseEntity<?> signInSeller(@RequestBody @Valid LoginRq rq) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Executing RESTfulService [POST /api/auth/seller/signin]");
        }

        return new ResponseEntity<>(sellerService.signInSeller(rq.getEmail(), rq.getPassword()), HttpStatus.OK);
    }


    // ===========================================================
    // Retrieve Logged-in Seller
    // ===========================================================
    @GetMapping("/seller-me")
    public ResponseEntity<?> retrieveSeller() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Executing RESTfulService [GET /find/seller]");
        }
        return new ResponseEntity<>(sellerService.retrieveSeller(), HttpStatus.OK);
    }

    // ===========================================================
    // Retrieve All Sellers (Admin Only)
    // ===========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sellers")
    public ResponseEntity<?> retrieveSellers() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Executing RESTfulService [GET /sellers]");
        }
        return new ResponseEntity<>(sellerService.retrieveSellers(), HttpStatus.OK);
    }

    // ===========================================================
    // Retrieve Seller Carts
    // ===========================================================
    @GetMapping("/find/seller/carts")
    public ResponseEntity<?> retrieveSellerCarts() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Executing RESTfulService [GET /find/seller/carts]");
        }
        return new ResponseEntity<>(sellerService.retrieveSellerCarts(), HttpStatus.OK);
    }

    // ===========================================================
    // Delete Seller (Admin Only)
    // ===========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/seller/{id}")
    public ResponseEntity<?> deleteSeller(@PathVariable("id") String id) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Executing RESTfulService [DELETE /delete/seller/{id}]");
        }
        return new ResponseEntity<>(sellerService.deleteSeller(id), HttpStatus.OK);
    }

    // ===========================================================
    // Forgot Password (Send Reset Link)
    // ===========================================================
    @PostMapping("/auth/seller/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Executing RESTfulService [POST /auth/seller/forgot-password]");
        }
        return new ResponseEntity<>(sellerService.forgotPassword(email), HttpStatus.OK);
    }

    // ===========================================================
    // Reset Password (with Token)
    // ===========================================================
    @PostMapping("/auth/seller/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
                                           @RequestParam("newPassword") String newPassword) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Executing RESTfulService [POST /auth/seller/reset-password]");
        }
        return new ResponseEntity<>(sellerService.resetPassword(token, newPassword), HttpStatus.OK);
    }
}
