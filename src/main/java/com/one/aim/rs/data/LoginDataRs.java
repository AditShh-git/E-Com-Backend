package com.one.aim.rs.data;

import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDataRs extends BaseDataRs {

	private static final long serialVersionUID = 9207579984347254263L;

	private String accessToken;

	private String refreshToken;

	private String type = "Bearer";

	private Long empId;

	private String username;

	private String fullname;

	private String email;

	public LoginDataRs(String message, String accessToken, String refreshToken, Long empId, String username,
			String fullname, String email) {
		super(message);
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.empId = empId;
		this.username = username;
		this.fullname = fullname;
		this.email = email;
	}

	public LoginDataRs(String message, String accessToken, String refreshToken, Long empId, String profileId,
			String username) {
		super(message);
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.empId = empId;
		this.username = username;
	}

	public LoginDataRs(String message) {
		super(message);
	}

}
