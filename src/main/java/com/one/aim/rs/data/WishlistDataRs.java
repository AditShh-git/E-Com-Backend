package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.bo.CartBO;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WishlistDataRs extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private List<CartBO> wishlist;

	public WishlistDataRs(String message) {
		super(message);
	}

	public WishlistDataRs(String message, List<CartBO> wishlist) {
		super(message);
		this.wishlist = wishlist;
	}

}
