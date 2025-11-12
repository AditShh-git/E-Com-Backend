package com.one.aim.service;

import com.one.vm.core.BaseRs;

public interface WishlistService {
	
	public BaseRs addToWishlist(String cartId) throws Exception;
	
	public BaseRs getUserWishlist() throws Exception;
	
	public BaseRs deleteUserWishlist(String wishId) throws Exception;

}
