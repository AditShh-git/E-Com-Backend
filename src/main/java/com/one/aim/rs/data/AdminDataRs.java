package com.one.aim.rs.data;

import com.one.aim.rs.AdminRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminDataRs extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private AdminRs admin;

	public AdminDataRs(String message) {
		super(message);
	}

	public AdminDataRs(String message, AdminRs admin) {
		super(message);
		this.admin = admin;
	}

}
