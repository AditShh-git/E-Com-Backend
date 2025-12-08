package com.one.aim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.service.WishlistService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    public ResponseEntity<?> add(@PathVariable Long productId) throws Exception {
        return ResponseEntity.ok(wishlistService.add(productId));
    }

    @GetMapping
    public ResponseEntity<?> getAll() throws Exception {
        return ResponseEntity.ok(wishlistService.getAll());
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> remove(@PathVariable Long productId) throws Exception {
        return ResponseEntity.ok(wishlistService.remove(productId));
    }

    @PostMapping("/move-to-cart/{productId}")
    public ResponseEntity<?> moveToCart(@PathVariable Long productId) throws Exception {
        return ResponseEntity.ok(wishlistService.moveToCart(productId));
    }

    @GetMapping("/count")
    public ResponseEntity<?> count() throws Exception {
        return ResponseEntity.ok(wishlistService.count());
    }
}
