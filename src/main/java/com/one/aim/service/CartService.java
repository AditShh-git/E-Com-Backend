package com.one.aim.service;

import com.one.aim.rq.CartRq;
import com.one.vm.core.BaseRs;

public interface CartService {

    BaseRs addProductToCart(Long productId) throws Exception;

    BaseRs updateQuantity(Long cartId, int quantity) throws Exception;

    BaseRs removeFromCart(Long cartId) throws Exception;

    BaseRs getMyCart() throws Exception;

    BaseRs placeOrder() throws Exception;
}
