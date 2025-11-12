package com.one.aim.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.VendorBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.helper.VendorHelper;
import com.one.aim.mapper.SellerMapper;
import com.one.aim.mapper.VendorMapper;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.VendorRepo;
import com.one.aim.rq.VendorRq;
import com.one.aim.rs.SellerRs;
import com.one.aim.rs.VendorRs;
import com.one.aim.rs.data.SellerDataRs;
import com.one.aim.rs.data.VendorDataRs;
import com.one.aim.rs.data.VendorDataRsList;
import com.one.aim.service.FileService;
import com.one.aim.service.VendorService;
import com.one.constants.StringConstants;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VendorServiceImpl implements VendorService {

	@Autowired
	VendorRepo vendorRepo;

	@Autowired
	AdminRepo adminRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	FileService fileService;

	// Sign Up
	@Override
	public BaseRs saveVendor(VendorRq rq) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing saveCompany(CompanyRq) ->");
		}

		List<String> errors = VendorHelper.validateAdmin(rq);

		if (Utils.isNotEmpty(errors)) {
			log.error(ErrorCodes.EC_INVALID_INPUT);
			return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
		}
		String docId = Utils.getValidString(rq.getDocId());
		String message = StringConstants.EMPTY;
		VendorBO vendorBO = null;
		if (Utils.isNotEmpty(docId)) { // UPDATE
			long id = Long.parseLong(docId);
			Optional<VendorBO> optAdminBO = vendorRepo.findById(id);
			vendorBO = optAdminBO.get();
			if (vendorBO == null) {
				log.error(ErrorCodes.EC_USER_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_USER_NOT_FOUND);
			}
//			AdminBO adminBO = adminRepo.findByIdAndUsername(AuthUtils.findLoggedInUser().getDocId(),
//					AuthUtils.findLoggedInUser().getUserName());
//			
			AdminBO adminBO = adminRepo.findByIdAndFullName(AuthUtils.findLoggedInUser().getDocId(),
					AuthUtils.findLoggedInUser().getFullName());
			boolean verified = Boolean.parseBoolean(rq.getIsVarified()); 
			
			if (null == adminBO && verified) {
				log.error(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
				return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
			} else {
				vendorBO.setVarified(verified);
			}
		} else {
			vendorBO = new VendorBO(); // SAVE
			message = MessageCodes.MC_SAVED_SUCCESSFUL;
		}
		String email = Utils.getValidString(rq.getEmail());
		if (!email.equals(Utils.getValidString(vendorBO.getEmail()))) {
			vendorBO.setEmail(email);
		}
		String userName = Utils.getValidString(rq.getUserName());
		if (!userName.equals(vendorBO.getFullName())) {
			vendorBO.setFullName(userName);
		}
		String phoneNo = Utils.getValidString(rq.getPhoneNo());
		if (!phoneNo.equals(vendorBO.getPhoneNo())) {
			vendorBO.setPhoneNo(phoneNo);
		}
		String gst = Utils.getValidString(rq.getGst());
		if (!gst.equals(vendorBO.getGst())) {
			vendorBO.setGst(gst);
		}
		String adhaar = Utils.getValidString(rq.getAdhaar());
		if (!adhaar.equals(vendorBO.getAdhaar())) {
			vendorBO.setAdhaar(adhaar);
		}
		String panCard = Utils.getValidString(rq.getPancard());
		if (!panCard.equals(vendorBO.getPhoneNo())) {
			vendorBO.setPancard(panCard);
		}
		String rawPassword = Utils.getValidString(rq.getPassword());
		String existingEncodedPassword = vendorBO.getPassword();

		if (!passwordEncoder.matches(rawPassword, existingEncodedPassword)) {
			String hashedPassword = passwordEncoder.encode(rawPassword);
			System.out.println("Hashed Password: " + hashedPassword);
			vendorBO.setPassword(hashedPassword);
		}
		vendorBO.setRoll("VENDOR");
		// adminBO.setAtts(fileService.prepareAttBOs(rq.getElExemptionAtts(), null));
		if (rq.getImage() != null) {
			vendorBO.setImage(rq.getImage()); // ✅ convert file to byte[]
		}
		vendorRepo.save(vendorBO);
		VendorRs adminRs = VendorMapper.mapToVendorRs(vendorBO);
		return ResponseUtils.success(new VendorDataRs(message, adminRs));
	}

//	public AdminBO getAdminBOById(Long id) {
//		Optional<AdminBO> admin = adminRepo.findById(id);
//        return admin.orElse(null);
//	}

	public BaseRs retrieveVendor() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing retrieveUser() ->");
		}
		try {
			Optional<VendorBO> optAdmin = vendorRepo.findById(AuthUtils.findLoggedInUser().getDocId());
			if (optAdmin.isEmpty()) {
				log.error(ErrorCodes.EC_VENDOR_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_VENDOR_NOT_FOUND);
			}
			VendorBO adminBO = optAdmin.get();
			VendorRs adminRs = VendorMapper.mapToVendorRs(adminBO);
			String message = MessageCodes.MC_RETRIEVED_SUCCESSFUL;
			return ResponseUtils.success(new VendorDataRs(message, adminRs));
		} catch (Exception e) {
			log.error("Exception retrieveUser() ->" + e);
			return null;
		}
	}

	public BaseRs retrieveVendors() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing retrieveVendors() ->");
		}
		try {
			Optional<AdminBO> optAdmin = adminRepo.findById(AuthUtils.findLoggedInUser().getDocId());
			if (optAdmin.isEmpty()) {
				log.error(ErrorCodes.EC_ADMIN_NOT_FOUND);
				return ResponseUtils.failure(ErrorCodes.EC_ADMIN_NOT_FOUND);
			}
			List<VendorBO> vendorBOs = vendorRepo.findAll();
			List<VendorRs> vendorRsList = VendorMapper.mapToVendorRsList(vendorBOs);
			String message = MessageCodes.MC_RETRIEVED_SUCCESSFUL;
			return ResponseUtils.success(new VendorDataRsList(message, vendorRsList));
		} catch (Exception e) {
			log.error("Exception retrieveVendors() ->" + e);
			return null;
		}
	}

	@Override
	public BaseRs deleteVendor(String id) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing deleteSeller(id) ->");
		}
		try {
			Optional<VendorBO> optVendor = vendorRepo.findById(Long.valueOf(id));
			Optional<AdminBO> optAdmin = adminRepo.findById(AuthUtils.findLoggedInUser().getDocId());
			if (optAdmin.isEmpty() && optVendor.isEmpty()) {
				log.error(ErrorCodes.EC_ACCESS_DENIED);
				return ResponseUtils.failure(ErrorCodes.EC_ACCESS_DENIED);
			}
			VendorBO vendorBO = optVendor.get();
			vendorRepo.deleteById(Long.valueOf(id));
			VendorRs vendorRs = VendorMapper.mapToVendorRs(vendorBO);
			String message = MessageCodes.MC_DELETED_SUCCESSFUL;
			return ResponseUtils.success(new VendorDataRs(message, vendorRs));
		} catch (Exception e) {
			log.error("Exception deleteSeller(id) ->" + e);
			return null;
		}
	}

//@Override
//public AdminBO getAdminBOById(Long id) {
//	// TODO Auto-generated method stub
//	return null;
//}
}
