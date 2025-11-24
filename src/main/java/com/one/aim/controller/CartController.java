package com.one.aim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.rq.CartRq;
import com.one.aim.service.CartService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/cart")
@Slf4j
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ==========================================================
    // ADD PRODUCT TO CART
    // ==========================================================
    @PostMapping("/add")
    public ResponseEntity<?> addProductToCart(@RequestParam Long productId) throws Exception {

        log.debug("Executing [POST /api/cart/add]");
        return ResponseEntity.ok(cartService.addProductToCart(productId));
    }

    // ==========================================================
    // UPDATE CART QUANTITY
    // ==========================================================
    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(
            @RequestParam Long cartId,
            @RequestParam int quantity
    ) throws Exception {

        log.debug("Executing [PUT /api/cart/update]");
        return ResponseEntity.ok(cartService.updateQuantity(cartId, quantity));
    }

    // ==========================================================
    // REMOVE ITEM FROM CART
    // ==========================================================
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam Long cartId) throws Exception {

        log.debug("Executing [DELETE /api/cart/remove]");
        return ResponseEntity.ok(cartService.removeFromCart(cartId));
    }

    // ==========================================================
    // GET MY CART
    // ==========================================================
    @GetMapping("/my")
    public ResponseEntity<?> getMyCart() throws Exception {

        log.debug("Executing [GET /api/cart/my]");
        return ResponseEntity.ok(cartService.getMyCart());
    }

//    // ==========================================================
//    // PLACE ORDER (AFTER CART)
//    // ==========================================================
//    @PostMapping("/place-order")
//    public ResponseEntity<?> placeOrder() throws Exception {
//
//        log.debug("Executing [POST /api/cart/place-order]");
//        return ResponseEntity.ok(cartService.placeOrder());
//    }
}
