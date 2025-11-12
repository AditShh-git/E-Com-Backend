package com.one.utils;

import java.io.Serializable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.one.aim.rs.UserRs;
import com.one.service.impl.UserDetailsImpl;

public class AuthUtils implements Serializable {

	private static final long serialVersionUID = -5948653276588477749L;

	public static UserRs findLoggedInUser() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// String currentPrincipalName = authentication.getName();

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		UserRs user = new UserRs(userDetails.getUsername(), userDetails.getId());
		return user;
	}

}