package com.one.aim.service;

import com.one.aim.bo.SellerBO;
import com.one.aim.rq.SellerFilterRequest;
import com.one.aim.rq.SellerRq;
import com.one.aim.rq.UpdateRq;
import com.one.vm.core.BaseRs;

public interface SellerService {

    // ===========================================================
    // SELLER SIGN-UP
    // ===========================================================
    BaseRs saveSeller(SellerRq rq) throws Exception;

    // ===========================================================
    // RETRIEVE SINGLE SELLER (Logged-in seller)
    // ===========================================================
    BaseRs retrieveSeller() throws Exception;

    // ===========================================================
    // RETRIEVE ALL SELLERS (Admin Only)
    // ===========================================================
    BaseRs retrieveSellers() throws Exception;


    // ===========================================================
    // RETRIEVE SELLER CARTS (Seller-only)
    // ===========================================================
    BaseRs retrieveSellerCarts() throws Exception;

    // ===========================================================
    // DELETE SELLER (Admin Only)
    // ===========================================================
    BaseRs deleteSeller(String id) throws Exception;

//    // ===========================================================
//    // EMAIL VERIFICATION (Signup + Email change)
//    // ===========================================================
//    void sendVerificationEmail(SellerBO seller);
//
//    BaseRs verifyEmail(String token) throws Exception;

    // ===========================================================
    // ADMIN APPROVAL EMAIL
    // ===========================================================
//    void sendAdminApprovalEmail(SellerBO seller);

    // ===========================================================
    // UPDATE SELLER PROFILE
    // ===========================================================
    BaseRs updateSellerProfile(String email, SellerRq rq) throws Exception;
}