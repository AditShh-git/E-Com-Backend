package com.one.aim.service;

import com.one.vm.core.BaseRs;

public interface WishlistService {

    // Add a product to wishlist
    BaseRs add(Long productId) throws Exception;

    // Get user's full wishlist
    BaseRs getAll() throws Exception;

    // Remove a product from wishlist
    BaseRs remove(Long productId) throws Exception;

    // Move wishlist item to cart
    BaseRs moveToCart(Long productId) throws Exception;

    // Wishlist item count (for badge)
    BaseRs count() throws Exception;
}

