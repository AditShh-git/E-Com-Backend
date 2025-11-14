package com.one.aim.service.impl;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.one.aim.bo.AdminBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.helper.AdminHelper;
import com.one.aim.mapper.AdminMapper;
import com.one.aim.repo.AdminRepo;
import com.one.aim.rq.AdminRq;
import com.one.aim.rs.AdminRs;
import com.one.aim.rs.data.AdminDataRs;
import com.one.aim.service.AdminService;
import com.one.aim.service.FileService;
import com.one.constants.StringConstants;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    // =========================================================
    // ADMIN SIGNUP / UPDATE
    // =========================================================
    @Override
    public BaseRs saveAdmin(AdminRq rq) throws Exception {

        log.debug("Executing saveAdmin()");

        List<String> errors = AdminHelper.validateAdmin(rq);
        if (Utils.isNotEmpty(errors)) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
        }

        AdminBO adminBO;
        String docId = Utils.getValidString(rq.getDocId());
        String message;

        // --------------------------
        // UPDATE
        // --------------------------
        if (Utils.isNotEmpty(docId)) {

            adminBO = adminRepo.findById(Long.parseLong(docId))
                    .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_ADMIN_NOT_FOUND));

            message = MessageCodes.MC_UPDATED_SUCCESSFUL;

        } else {
            // --------------------------
            // CREATE
            // --------------------------
            adminBO = new AdminBO();
            adminBO.setRole("ADMIN");
            message = MessageCodes.MC_SAVED_SUCCESSFUL;
        }

        // Email
        String email = Utils.getValidString(rq.getEmail());
        if (!email.equals(Utils.getValidString(adminBO.getEmail()))) {
            adminBO.setEmail(email);
        }

        // Name
        String fullName = Utils.getValidString(rq.getUserName());
        if (!fullName.equals(Utils.getValidString(adminBO.getFullName()))) {
            adminBO.setFullName(fullName);
        }

        // Phone
        String phone = Utils.getValidString(rq.getPhoneNo());
        if (!phone.equals(Utils.getValidString(adminBO.getPhoneNo()))) {
            adminBO.setPhoneNo(phone);
        }

        // Password (correct fix)
        String rawPassword = Utils.getValidString(rq.getPassword());

        if (Utils.isNotEmpty(rawPassword)) {
            if (!passwordEncoder.matches(rawPassword, Utils.getValidString(adminBO.getPassword()))) {
                adminBO.setPassword(passwordEncoder.encode(rawPassword));
            }
        }

        // Image
        if (rq.getImage() != null) {
            adminBO.setImage(rq.getImage());
        }

        adminRepo.save(adminBO);

        return ResponseUtils.success(
                new AdminDataRs(message, AdminMapper.mapToAdminRs(adminBO))
        );
    }


    // =========================================================
    // GET LOGGED-IN ADMIN PROFILE
    // =========================================================
    @Override
    public BaseRs retrieveAdmin() throws Exception {

        log.debug("Executing retrieveAdmin()");

        Long loggedId = AuthUtils.findLoggedInUser().getDocId();
        AdminBO adminBO = adminRepo.findById(loggedId)
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_ADMIN_NOT_FOUND));

        return ResponseUtils.success(
                new AdminDataRs(
                        MessageCodes.MC_RETRIEVED_SUCCESSFUL,
                        AdminMapper.mapToAdminRs(adminBO)
                )
        );
    }


    // =========================================================
    // DELETE ADMIN
    // =========================================================
    @Override
    public BaseRs deleteAdmin(String id) throws Exception {

        log.debug("Executing deleteAdmin()");

        AdminBO adminBO = adminRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_ADMIN_NOT_FOUND));

        adminRepo.delete(adminBO);

        return ResponseUtils.success(
                new AdminDataRs(
                        MessageCodes.MC_DELETED_SUCCESSFUL,
                        AdminMapper.mapToAdminRs(adminBO)
                )
        );
    }

//@Override
//public AdminBO getAdminBOById(Long id) {
//	// TODO Auto-generated method stub
//	return null;
//}
}
