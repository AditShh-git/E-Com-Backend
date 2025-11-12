package com.one.aim.helper;

import java.util.ArrayList;
import java.util.List;

import com.one.aim.constants.ErrorCodes;
import com.one.aim.rq.AdminRq;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminHelper {

	public static List<String> validateAdmin(AdminRq rq) {

		if (log.isDebugEnabled()) {
			log.debug("Executing validateAdmin(AdminRq) ->");
		}

		List<String> errors = new ArrayList<>();
		try {
			if (Utils.isEmpty(rq.getUserName())) {
				log.error(ErrorCodes.EC_REQUIRED_USERNAME);
				errors.add(ErrorCodes.EC_REQUIRED_USERNAME);
			}
			String email = Utils.getValidString(rq.getEmail()).toLowerCase();
			if (Utils.isEmpty(rq.getEmail())) {
				log.error(ErrorCodes.EC_REQUIRED_EMAIL);
				errors.add(ErrorCodes.EC_REQUIRED_EMAIL);
			} else if (!Utils.isValidEmail(email)) {
				log.error(ErrorCodes.EC_INVALID_EMAIL);
				errors.add(ErrorCodes.EC_INVALID_EMAIL);
			}
			if (Utils.isEmpty(rq.getPassword())) {
				log.error(ErrorCodes.EC_REQUIRED_PASSWORD);
				errors.add(ErrorCodes.EC_REQUIRED_PASSWORD);
			}
		} catch (Exception e) {
			log.error("Exception in validateAdmin(AdminRq) - " + e);
			errors.add(ErrorCodes.EC_INVALID_INPUT);
		}
		return errors;
	}

}
