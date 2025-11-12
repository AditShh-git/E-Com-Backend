package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.rs.CartMaxRs;
import com.one.aim.rs.CartRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartMaxDataRsList extends BaseDataRs {
	private static final long serialVersionUID = 1L;

	private List<CartMaxRs> carts;

	public CartMaxDataRsList(String message) {
		super(message);
	}

	public CartMaxDataRsList(String message, List<CartMaxRs> carts) {
		super(message);
		this.carts = carts;
	}

}
