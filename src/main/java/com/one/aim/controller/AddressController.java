package com.one.aim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.bo.AddressBO;
import com.one.aim.rq.AddressRq;
import com.one.aim.service.AddressService;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api")
@Slf4j
public class AddressController {

	@Autowired
	private AddressService addressService;

	@PostMapping("/adddress/save")
	public ResponseEntity<?> saveCart(@RequestBody AddressRq rq) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Executing RESTfulService [POST /user]");
		}
		return new ResponseEntity<>(addressService.saveAddress(rq), HttpStatus.OK);
	}
	
	
	
	
//	@GetMapping("/{id}")
//	public ResponseEntity<AddressBO> getAddressById(@PathVariable Long id) {
//	    try {
//	        if (log.isDebugEnabled()) {
//	            log.debug("Executing getAddressById(id={}) ->", id);
//	        }
//
//	        AddressBO address = addressService.getAddressById(id);
//	        return new ResponseEntity<>(address, HttpStatus.OK);
//
//	    } catch (Exception e) {
//	        log.error("Error fetching address by ID: {}", id, e);
//	        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
//	    }
//	}
	
	

	
	
	
	@GetMapping("/me")
	public ResponseEntity<BaseRs> getMyAddress() {
	    try {
	        if (log.isDebugEnabled()) {
	            log.debug("Executing getMyAddress ->");
	        }

	        BaseRs response = addressService.getAddressOfLoggedInUser();
	        return new ResponseEntity<>(response, HttpStatus.OK);

	    } catch (Exception e) {
	        log.error("Error fetching logged-in user address", e);
	        return new ResponseEntity<>(ResponseUtils.failure("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	
}
