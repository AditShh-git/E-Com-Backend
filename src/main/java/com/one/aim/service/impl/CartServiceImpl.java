package com.one.aim.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.one.aim.bo.*;
import com.one.aim.repo.*;
import com.one.aim.rs.OrderIdRs;
import com.one.aim.service.AdminSettingService;
import com.one.aim.service.InvoiceService;
import com.one.vm.core.BaseDataRs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.mapper.CartMapper;
import com.one.aim.rq.CartRq;
import com.one.aim.rs.CartMaxRs;
import com.one.aim.rs.CartRs;
import com.one.aim.rs.data.CartDataRs;
import com.one.aim.rs.data.CartDataRsList;
import com.one.aim.rs.data.CartMaxDataRsList;
import com.one.aim.service.CartService;
import com.one.aim.service.FileService;
import com.one.constants.StringConstants;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepo cartRepo;
    private final AddressRepo  addressRepo;
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final InvoiceService  invoiceService;
    private final UserActivityService userActivityService;
    private final FileService fileService;

    // ==========================================================
    // ADD PRODUCT TO CART  (Flipkart Style)
    // ==========================================================
    @Override
    @Transactional
    public BaseRs addProductToCart(Long productId) throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        UserBO user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductBO product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Find active cart entry  UPDATED
        Optional<CartBO> existingOpt =
                cartRepo.findByUserAddToCart_IdAndProduct_IdAndEnabled(userId, productId, true);

        if (existingOpt.isPresent()) {
            CartBO existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + 1);
            cartRepo.save(existing);

            return ResponseUtils.success(
                    new CartDataRs("Quantity updated",
                            CartMapper.mapToCartRs(existing, fileService))
            );
        }

        // Create new active cart entry
        CartBO cart = new CartBO();
        cart.setUserAddToCart(user);
        cart.setProduct(product);
        cart.setPname(product.getName());
        cart.setPrice(product.getPrice() == null ? 0L : product.getPrice().longValue());
        cart.setQuantity(1);
        cart.setEnabled(true);
        cart.setSellerId(product.getSeller().getSellerId());

        cartRepo.save(cart);

        userActivityService.log(
                userId,
                "ADD_TO_CART",
                "Added product ID: " + productId
        );


        return ResponseUtils.success(
                new CartDataRs("Added to cart",
                        CartMapper.mapToCartRs(cart, fileService))
        );
    }

    // ==========================================================
    // UPDATE QUANTITY
    // ==========================================================
    @Override
    @Transactional
    public BaseRs updateQuantity(Long cartId, int quantity) throws Exception {

        if (quantity < 1)
            return ResponseUtils.failure("INVALID_QTY", "Quantity must be >= 1");

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        CartBO cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (!cart.getUserAddToCart().getId().equals(userId)) {
            return ResponseUtils.failure("NOT_ALLOWED", "You cannot update others cart");
        }

        cart.setQuantity(quantity);
        cartRepo.save(cart);

        // USER ACTIVITY LOG
        userActivityService.log(
                userId,
                "UPDATE_CART",
                "Updated quantity for cart ID: " + cartId + " to " + quantity
        );

        return ResponseUtils.success(
                new CartDataRs("Quantity updated",
                        CartMapper.mapToCartRs(cart, fileService))
        );
    }


    // ==========================================================
    // REMOVE CART ITEM
    // ==========================================================
    @Override
    @Transactional
    public BaseRs removeFromCart(Long cartId) throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        CartBO cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (!cart.getUserAddToCart().getId().equals(userId)) {
            return ResponseUtils.failure("NOT_ALLOWED", "You cannot remove others cart");
        }

        cartRepo.delete(cart);

        // USER ACTIVITY LOG
        userActivityService.log(
                userId,
                "REMOVE_FROM_CART",
                "Removed cart ID: " + cartId
        );

        return ResponseUtils.success("Removed from cart");
    }


    // ==========================================================
    // LIST USER CART
    // ==========================================================
    @Override
    public BaseRs getMyCart() throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        // Only show ACTIVE cart items   FIXED
        List<CartBO> cartList =
                cartRepo.findAllByUserAddToCart_IdAndEnabled(userId, true);

        return ResponseUtils.success(
                new CartDataRsList("Cart loaded",
                        CartMapper.mapToCartRsList(cartList, fileService))
        );
    }
}
