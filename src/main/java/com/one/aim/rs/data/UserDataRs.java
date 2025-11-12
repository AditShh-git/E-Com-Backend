package com.one.aim.rs.data;

import com.one.aim.rs.UserRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDataRs extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private UserRs user;

	public UserDataRs(String message) {
		super(message);
	}

	public UserDataRs(String message, UserRs user) {
		super(message);
		this.user = user;
	}

}
