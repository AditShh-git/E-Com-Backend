package com.one.aim.service;

import com.one.aim.rq.AdminRq;
import com.one.vm.core.BaseRs;

public interface AdminService {

    // CREATE ADMIN
    BaseRs createAdmin(AdminRq rq) throws Exception;

    // UPDATE ADMIN
    BaseRs updateAdmin(AdminRq rq) throws Exception;

    // GET LOGGED-IN ADMIN
    BaseRs retrieveAdmin() throws Exception;

    // DELETE ADMIN
    BaseRs deleteAdmin(String id) throws Exception;

}
