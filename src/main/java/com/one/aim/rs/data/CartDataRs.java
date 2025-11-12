package com.one.aim.rs.data;

import com.one.aim.rs.CartRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartDataRs extends BaseDataRs {
	private static final long serialVersionUID = 1L;

	private CartRs cart;

	public CartDataRs(String message) {
		super(message);
	}

	public CartDataRs(String message, CartRs cart) {
		super(message);
		this.cart = cart;
	}

}
