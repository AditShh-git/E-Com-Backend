package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.rs.VendorRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VendorDataRsList extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private List<VendorRs> vendors;

	public VendorDataRsList(String message) {
		super(message);
	}

	public VendorDataRsList(String message, List<VendorRs> vendors) {
		super(message);
		this.vendors = vendors;
	}

}
