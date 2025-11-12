package com.one.aim.rs.data;

import com.one.aim.rs.SellerRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerDataRs extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private SellerRs seller;

	public SellerDataRs(String message) {
		super(message);
	}

	public SellerDataRs(String message, SellerRs seller) {
		super(message);
		this.seller = seller;
	}

}
