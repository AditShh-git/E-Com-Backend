package com.one.aim.helper;

import java.util.ArrayList;
import java.util.List;

import com.one.aim.constants.ErrorCodes;
import com.one.aim.rq.AddressRq;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AddressHelper {

	public static List<String> validateAddress(AddressRq rq) {

		if (log.isDebugEnabled()) {
			log.debug("Executing validateAddress(AddressRq) ->");
		}

		List<String> errors = new ArrayList<>();
		try {
			if (Utils.isEmpty(rq.getFullName())) {
				log.error(ErrorCodes.EC_REQUIRED_FULL_ADDRESS);
				errors.add(ErrorCodes.EC_REQUIRED_FULL_ADDRESS);
			}
			if (Utils.isEmpty(rq.getStreet())) {
				log.error(ErrorCodes.EC_REQUIRED_STREET);
				errors.add(ErrorCodes.EC_REQUIRED_STREET);
			}
			if (Utils.isEmpty(rq.getCity())) {
				log.error(ErrorCodes.EC_REQUIRED_CITY);
				errors.add(ErrorCodes.EC_REQUIRED_CITY);
			}
			if (Utils.isEmpty(rq.getState())) {
				log.error(ErrorCodes.EC_REQUIRED_STATE);
				errors.add(ErrorCodes.EC_REQUIRED_STATE);
			}
			if (Utils.isEmpty(rq.getCountry())) {
				log.error(ErrorCodes.EC_REQUIRED_COUNTRY);
				errors.add(ErrorCodes.EC_REQUIRED_COUNTRY);
			}
			if (Utils.isEmpty(rq.getZip())) {
				log.error(ErrorCodes.EC_REQUIRED_ZIP);
				errors.add(ErrorCodes.EC_REQUIRED_ZIP);
			}
			if (Utils.isEmpty(rq.getPhone())) {
				log.error(ErrorCodes.EC_REQUIRED_PHONE);
				errors.add(ErrorCodes.EC_REQUIRED_PHONE);
			}

		} catch (Exception e) {
			log.error("Exception in validateAddress(AddressRq) - " + e);
			errors.add(ErrorCodes.EC_INVALID_INPUT);
		}
		return errors;
	}

}
