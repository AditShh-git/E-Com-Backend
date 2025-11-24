package com.one.aim.service;

import com.one.aim.rq.UpdateRq;

import com.one.aim.rq.UserFilterRequest;
import com.one.aim.rq.UserRq;
import com.one.vm.core.BaseRs;

public interface UserService {

    BaseRs saveUser(UserRq rq) throws Exception;

    BaseRs updateUserProfile(String email, UpdateRq request);

    //  Retrieve currently logged-in user
    BaseRs retrieveUser() throws Exception;

    //  Retrieve all users (admin only)
    BaseRs retrieveAllUser() throws Exception;


    //  Delete user (admin)
    BaseRs deleteUser(String id) throws Exception;

    BaseRs deleteMyAccount() throws Exception;


//    BaseRs verifyEmail(String token);
}
