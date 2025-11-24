package com.one.aim.service;

import com.one.aim.rq.UpdateRq;
import com.one.aim.rq.UserFilterRequest;
import com.one.aim.rq.UserRq;
import com.one.vm.core.BaseRs;

public interface UserService {

    // Create a new user
    BaseRs saveUser(UserRq rq) throws Exception;

    // Update user profile
    BaseRs updateUserProfile(String email, UpdateRq request);

    // Retrieve currently logged-in user
    BaseRs retrieveUser() throws Exception;

    // Retrieve all users (ADMIN only)
    BaseRs retrieveAllUser() throws Exception;

    // Delete a user (ADMIN)
    BaseRs deleteUser(String id) throws Exception;

    // Delete My Account (USER deletes own account) — from wishlist-address-delete branch
    BaseRs deleteMyAccount() throws Exception;
}
