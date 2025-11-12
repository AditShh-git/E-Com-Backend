package com.one.aim.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.one.aim.bo.AddressBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.helper.AddressHelper;
import com.one.aim.repo.AddressRepo;
import com.one.aim.rq.AddressRq;
import com.one.aim.rs.AddressDataRs;
import com.one.aim.rs.data.UserDataRs;
import com.one.aim.service.AddressService;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {

//	@Autowired
//	private UserRepo userRepo;

	@Autowired
	private AddressRepo addressRepo;

	@Override
	public BaseRs saveAddress(AddressRq rq) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing saveAddress(AddressRq) ->");
		}

		List<String> errors = AddressHelper.validateAddress(rq);

		if (Utils.isNotEmpty(errors)) {
			log.error(ErrorCodes.EC_INVALID_INPUT);
			return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
		}

	//	UserBO userBO = userRepo.findById(rq.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

		AddressBO address = new AddressBO();
		address.setFullName(rq.getFullName());
		address.setStreet(rq.getStreet());
		address.setCity(rq.getCity());
		address.setState(rq.getState());
		address.setZip(rq.getZip());
		address.setCountry(rq.getCountry());
		address.setPhone(rq.getPhone());
		address.setUserid(AuthUtils.findLoggedInUser().getDocId());
		addressRepo.save(address);
		String message = MessageCodes.MC_SAVED_SUCCESSFUL;
		return ResponseUtils.success(new UserDataRs(message));
	}
	
	
	@Override
	public AddressBO getAddressById(Long id) throws Exception {
	    if (log.isDebugEnabled()) {
	        log.debug("Executing getAddressById(id={}) ->", id);
	    }

	    return addressRepo.findById(id)
	            .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));
	}
	
	

	
	
	
	@Override
	public BaseRs getAddressOfLoggedInUser() throws Exception {
	    if (log.isDebugEnabled()) {
	        log.debug("Executing getAddressOfLoggedInUser ->");
	    }

	    // Current logged-in user ID
	    Long userId = AuthUtils.findLoggedInUser().getDocId();

	    // Get first address of logged-in user
	    AddressBO address = addressRepo.findByUserid(userId)
	            .stream()
	            .findFirst()
	            .orElseThrow(() -> new RuntimeException("Address not found for logged-in user"));

	    // Wrap into AddressDataRs
	    AddressDataRs data = new AddressDataRs("Address retrieved successfully");
	    data.setId(address.getId());
	    data.setFullName(address.getFullName());
	    data.setStreet(address.getStreet());
	    data.setCity(address.getCity());
	    data.setState(address.getState());
	    data.setZip(address.getZip());
	    data.setCountry(address.getCountry());
	    data.setPhone(address.getPhone());
	    data.setUserid(address.getUserid());

	    return ResponseUtils.success(data);
	}


}
