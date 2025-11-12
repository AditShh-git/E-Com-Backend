package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.rs.UserRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDataRsList extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private List<UserRs> users;

	public UserDataRsList(String message) {
		super(message);
	}

	public UserDataRsList(String message, List<UserRs> users) {
		super(message);
		this.users = users;
	}

}
