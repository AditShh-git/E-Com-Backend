package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.rs.CartRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartDataRsList extends BaseDataRs {
	private static final long serialVersionUID = 1L;

	private List<CartRs> carts;

	public CartDataRsList(String message) {
		super(message);
	}

	public CartDataRsList(String message, List<CartRs> carts) {
		super(message);
		this.carts = carts;
	}

}
