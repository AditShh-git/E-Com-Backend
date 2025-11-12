package com.one.aim.controller;

import com.one.aim.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/product")
@Slf4j
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    // ===========================================================
    // PUBLIC: VIEW PRODUCT DETAILS (NO AUTH REQUIRED)
    // ===========================================================
    @GetMapping("/{slug}")
    public ResponseEntity<?> getProductBySlug(@PathVariable String slug) throws Exception {
        log.debug("Executing RESTfulService [GET /api/public/product/{}]", slug);
        return ResponseEntity.ok(productService.getShareableProduct(slug));
    }

    // ===========================================================
    // PUBLIC: GET PRODUCT IMAGES (NO AUTH REQUIRED)
    // ===========================================================
    @GetMapping("/{productId}/images")
    public ResponseEntity<?> getProductImages(@PathVariable Long productId) throws Exception {
        log.debug("Executing RESTfulService [GET /api/public/product/{}/images]", productId);
        return ResponseEntity.ok(productService.getProductImages(productId));
    }
}