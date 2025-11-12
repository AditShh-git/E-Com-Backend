package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.rs.SellerRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerDataRsList extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private List<SellerRs> sellers;

	public SellerDataRsList(String message) {
		super(message);
	}

	public SellerDataRsList(String message, List<SellerRs> sellers) {
		super(message);
		this.sellers = sellers;
	}

}
