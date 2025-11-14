package com.one.aim.service;

import com.one.aim.rq.AdminRq;
import com.one.vm.core.BaseRs;

public interface AdminService {

    /**
     * Create or Update an Admin
     */
    BaseRs saveAdmin(AdminRq rq) throws Exception;

    /**
     * Get currently logged-in Admin profile
     */
    BaseRs retrieveAdmin() throws Exception;

    /**
     * Delete admin by ID
     */
    BaseRs deleteAdmin(String id) throws Exception;

}
