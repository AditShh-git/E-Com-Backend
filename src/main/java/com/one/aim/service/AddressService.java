package com.one.aim.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.one.aim.bo.AddressBO;
import com.one.aim.rq.AddressRq;
import com.one.vm.core.BaseRs;

public interface AddressService {

	public BaseRs saveAddress(AddressRq rq) throws Exception;
	
	
	AddressBO getAddressById(Long id) throws Exception;
	
	BaseRs getAddressOfLoggedInUser() throws Exception;


}
