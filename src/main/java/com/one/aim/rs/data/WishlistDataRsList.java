package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.rs.WishlistRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WishlistDataRsList extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private List<WishlistRs> wishlist;

	public WishlistDataRsList(String message) {
		super(message);
	}

	public WishlistDataRsList(String message, List<WishlistRs> wishlistRs) {
		super(message);
		this.wishlist = wishlistRs;
	}

}
