package com.one.aim.service;

import com.one.aim.rq.SellerRq;
import com.one.aim.rq.UpdateRq;
import com.one.vm.core.BaseRs;

public interface SellerService {

	public BaseRs saveSeller(SellerRq rq) throws Exception;

    BaseRs signInSeller(String email, String password) throws Exception;

    public BaseRs retrieveSeller() throws Exception;

	public BaseRs retrieveSellers() throws Exception;

	public BaseRs retrieveSellerCarts() throws Exception;

	public BaseRs deleteSeller(String id) throws Exception;

    BaseRs forgotPassword(String email) throws Exception;

    BaseRs resetPassword(String token, String newPassword) throws Exception;

//    BaseRs updateSellerProfile(UpdateRq request) throws Exception;

    BaseRs updateSellerProfile(String email, SellerRq rq) throws Exception;



}