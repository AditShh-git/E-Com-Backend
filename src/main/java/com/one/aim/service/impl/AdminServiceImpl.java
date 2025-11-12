package com.one.aim.service.impl;

import java.util.List;
import java.util.Optional;

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
public class AdminServiceImpl implements AdminService {

	@Autowired
	AdminRepo adminRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	FileService fileService;

	// Sign Up
	@Override
	public BaseRs saveAdmin(AdminRq rq) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing saveCompany(CompanyRq) ->");
		}

		List<String> errors = AdminHelper.validateAdmin(rq);

		if (Utils.isNotEmpty(errors)) {
			log.error(ErrorCodes.EC_INVALID_INPUT);
			return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
		}
		String docId = Utils.getValidString(rq.getDocId());
		String message = StringConstants.EMPTY;
		AdminBO adminBO = null;
		if (Utils.isNotEmpty(docId)) { // UPDATE
			long id = Long.parseLong(docId);
			Optional<AdminBO> optAdminBO = adminRepo.findById(id);
			adminBO = optAdminBO.get();
			if (adminBO == null) {
				log.error(ErrorCodes.EC_USER_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
			}
		} else {
			adminBO = new AdminBO(); // SAVE
			message = MessageCodes.MC_SAVED_SUCCESSFUL;
		}
		String email = Utils.getValidString(rq.getEmail());
		if (!email.equals(Utils.getValidString(adminBO.getEmail()))) {
			adminBO.setEmail(email);
		}
		String userName = Utils.getValidString(rq.getUserName());
		if (!userName.equals(adminBO.getFullName())) {
			adminBO.setFullName(userName);
		}
		String phoneNo = Utils.getValidString(rq.getPhoneNo());
		if (!phoneNo.equals(adminBO.getPhoneNo())) {
			adminBO.setPhoneNo(phoneNo);
		}
		String rawPassword = Utils.getValidString(rq.getPassword());
		String existingEncodedPassword = adminBO.getPassword();

		if (!passwordEncoder.matches(rawPassword, existingEncodedPassword)) {
			String hashedPassword = passwordEncoder.encode(rawPassword);
			System.out.println("Hashed Password: " + hashedPassword);
			adminBO.setPassword(hashedPassword);
		}
		adminBO.setRole("ADMIN");
		// adminBO.setAtts(fileService.prepareAttBOs(rq.getElExemptionAtts(), null));
		//adminBO.setImageid(rq.getImageid());
		if (rq.getImage() != null) {
			adminBO.setImage(rq.getImage()); // ✅ convert file to byte[]
		}
		adminRepo.save(adminBO);
		AdminRs adminRs = AdminMapper.mapToAdminRs(adminBO);
		return ResponseUtils.success(new AdminDataRs(message, adminRs));
	}

//	public AdminBO getAdminBOById(Long id) {
//		Optional<AdminBO> admin = adminRepo.findById(id);
//        return admin.orElse(null);
//	}

	public BaseRs retrieveAdmin() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing retrieveUser() ->");
		}
		try {
			Optional<AdminBO> optAdmin = adminRepo.findById(AuthUtils.findLoggedInUser().getDocId());
			if (optAdmin.isEmpty()) {
				log.error(ErrorCodes.EC_USER_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
			}
			AdminBO adminBO = optAdmin.get();
			AdminRs adminRs = AdminMapper.mapToAdminRs(adminBO);
			String message = MessageCodes.MC_RETRIEVED_SUCCESSFUL;
			return ResponseUtils.success(new AdminDataRs(message, adminRs));
		} catch (Exception e) {
			log.error("Exception retrieveUser() ->" + e);
			return null;
		}
	}

	@Override
	public BaseRs deleteAdmin(String id) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing deleteAdmin() ->");
		}
		try {
			Optional<AdminBO> optAdmin = adminRepo.findById(Long.valueOf(id));
			if (optAdmin.isEmpty()) {
				log.error(ErrorCodes.EC_ADMIN_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_ADMIN_NOT_FOUND);
			}
			AdminBO adminBO = optAdmin.get();
			adminRepo.deleteById(Long.valueOf(id));
			AdminRs adminRs = AdminMapper.mapToAdminRs(adminBO);
			String message = MessageCodes.MC_DELETED_SUCCESSFUL;
			return ResponseUtils.success(new AdminDataRs(message, adminRs));
		} catch (Exception e) {
			log.error("Exception deleteAdmin() ->" + e);
			return null;
		}
	}

//@Override
//public AdminBO getAdminBOById(Long id) {
//	// TODO Auto-generated method stub
//	return null;
//}
}
