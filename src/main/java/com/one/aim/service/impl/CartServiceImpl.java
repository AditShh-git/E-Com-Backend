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
    private final FileService fileService;   // REQUIRED for CartMapper

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

        // Find active cart entry
        Optional<CartBO> existingOpt =
                cartRepo.findByUserAddToCart_IdAndProduct_IdAndEnabledTrue(userId, productId);

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

        return ResponseUtils.success("Removed from cart");
    }

    // ==========================================================
    // LIST USER CART
    // ==========================================================
    @Override
    public BaseRs getMyCart() throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        List<CartBO> cartList = cartRepo.findAllByUserAddToCart_Id(userId);

        return ResponseUtils.success(
                new CartDataRsList("Cart loaded",
                        CartMapper.mapToCartRsList(cartList, fileService))
        );
    }

//    // ==========================================================
//// PLACE ORDER
//// ==========================================================
//    @Override
//    @Transactional
//    public BaseRs placeOrder() throws Exception {
//
//        Long userId = AuthUtils.findLoggedInUser().getDocId();
//
//        // ---- Fetch cart of current user ----
//        List<CartBO> cartList = cartRepo.findAllByUserAddToCart_Id(userId);
//
//        if (cartList.isEmpty())
//            return ResponseUtils.failure("EMPTY_CART", "Your cart is empty");
//
//        // ---- Compute total amount ----
//        long totalAmount = cartList.stream()
//                .mapToLong(c -> c.getPrice() * c.getQuantity())
//                .sum();
//
//        // ---- Fetch shipping address ----
//        AddressBO address = addressRepo.findByUserid(userId)
//                .stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("No shipping address found for user"));
//
//        UserBO user = userRepo.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // ---- Create Order ----
//        OrderBO order = new OrderBO();
//        order.setUser(user);
//        order.setShippingAddress(address);    // <---- THIS FIXES address_id null
//        order.setOrderTime(LocalDateTime.now());
//        order.setOrderStatus("PLACED");
//        order.setPaymentMethod("CASH_ON_DELIVERY");
//        order.setTotalAmount(totalAmount);
//        order.setInvoiceno("INV-" + System.currentTimeMillis());
//
//        // ---- Attach CartBO items correctly ----
//        for (CartBO cart : cartList) {
//
//            cart.setUserAddToCart(null);      // remove user link
//
//            cartRepo.save(cart);              // <---- IMPORTANT
//            // avoids: TransientObjectException
//            // makes CartBO "persistent" again
//
//            order.getCartItems().add(cart);   // add to order
//        }
//
//        // ---- Save Order ----
//        OrderBO savedOrder = orderRepo.save(order);
//
//        // ---- Generate Invoice ----
//        invoiceService.generateInvoice(savedOrder.getId());
//
//        // ---- Clear user's cart ----
//        // IMPORTANT: do NOT delete carts now because they belong to order
//        // only delete "user_addcart_id" relation
//        // already removed above, so nothing more required
//
//        return ResponseUtils.success(
//                new BaseDataRs("Order placed successfully!", new OrderIdRs(savedOrder.getId()))
//        );
//
//
//    }


}
