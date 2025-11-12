package com.one.security.jwt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.aim.constants.ErrorCodes;
import com.one.constants.StringConstants;
import com.one.exception.NotAuthorizedException;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint, Serializable {

	private static final long serialVersionUID = -7858869558953243875L;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException, NotAuthorizedException {
		try {
			System.out.println("Hell--1");
			String errorCode = ErrorCodes.EC_UNAUTHORIZED_ACCESS;
			if (null != request.getAttribute(StringConstants.JWT_INVALID_TOKEN)) {
				errorCode = String.valueOf(request.getAttribute(StringConstants.JWT_INVALID_TOKEN));
			} else if (null != request.getAttribute(StringConstants.JWT_BAD_CREDENTIALS)) {
				errorCode = String.valueOf(request.getAttribute(StringConstants.JWT_BAD_CREDENTIALS));
			}
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			BaseRs baseRs = ResponseUtils.failure(errorCode);
			OutputStream out = response.getOutputStream();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(out, baseRs);
			out.flush();
		} catch (Exception e) {
			log.error("Exception in  commence: {}", e.getMessage());
		}
	}
}