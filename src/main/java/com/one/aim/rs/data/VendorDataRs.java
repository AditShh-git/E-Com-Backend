package com.one.aim.rs.data;

import com.one.aim.rs.VendorRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VendorDataRs extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private VendorRs ventor;

	public VendorDataRs(String message) {
		super(message);
	}

	public VendorDataRs(String message, VendorRs ventor) {
		super(message);
		this.ventor = ventor;
	}

}
