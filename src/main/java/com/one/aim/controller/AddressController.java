package com.one.aim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.one.aim.bo.AddressBO;
import com.one.aim.rq.AddressRq;
import com.one.aim.service.AddressService;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/address")
@Slf4j
public class AddressController {

    @Autowired
    private AddressService addressService;

    // =========================================================
    // ADD NEW ADDRESS
    // =========================================================
    @PostMapping("/save")
    public ResponseEntity<?> saveAddress(@RequestBody AddressRq rq) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing POST /api/address");
        }

        return ResponseEntity.ok(addressService.saveAddress(rq));
    }


    // =========================================================
    // GET DEFAULT ADDRESS
    // =========================================================
    @GetMapping("/default")
    public ResponseEntity<?> getDefaultAddress() {

        try {
            if (log.isDebugEnabled()) log.debug("GET /api/address/default");

            return ResponseEntity.ok(addressService.getAddressOfLoggedInUser());

        } catch (Exception e) {
            log.error("Error fetching default address", e);
            return ResponseEntity.internalServerError()
                    .body(ResponseUtils.failure("Internal server error"));
        }
    }


    // =========================================================
    // GET ALL ADDRESSES OF LOGGED-IN USER
    // =========================================================
    @GetMapping
    public ResponseEntity<?> getAllAddresses() {

        try {
            if (log.isDebugEnabled()) log.debug("GET /api/address");

            return ResponseEntity.ok(addressService.getAllAddressesOfLoggedUser());

        } catch (Exception e) {
            log.error("Error fetching address list", e);
            return ResponseEntity.internalServerError()
                    .body(ResponseUtils.failure("Internal server error"));
        }
    }


    // =========================================================
    // SET DEFAULT ADDRESS
    // =========================================================
    @PutMapping("/default/{addressId}")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long addressId) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("PUT /api/address/default/{}", addressId);
        }

        return ResponseEntity.ok(addressService.setDefaultAddress(addressId));
    }


    // =========================================================
    // UPDATE ADDRESS
    // =========================================================
    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable Long addressId,
                                           @RequestBody AddressRq rq) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("PUT /api/address/{}", addressId);
        }

        return ResponseEntity.ok(addressService.updateAddress(addressId, rq));
    }


    // =========================================================
    // DELETE ADDRESS
    // =========================================================
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("DELETE /api/address/{}", addressId);
        }

        return ResponseEntity.ok(addressService.deleteAddress(addressId));
    }
}
