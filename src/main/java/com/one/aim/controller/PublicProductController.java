package com.one.aim.controller;

import com.one.aim.bo.ProductBO;
import com.one.aim.rs.ProductRs;
import com.one.aim.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<?> getShareProduct(@PathVariable String slug) throws Exception {

        String shareMessage = productService.getShareableProduct(slug);

        Map<String, Object> response = new HashMap<>();
        response.put("shareMessage", shareMessage);

        return ResponseEntity.ok(response);
    }



    // ===========================================================
    // PUBLIC: GET PRODUCT IMAGES (NO AUTH REQUIRED)
    // ===========================================================
    @GetMapping("/{productId}/images")
    public ResponseEntity<?> getProductImages(@PathVariable Long productId) throws Exception {
        log.debug("Executing RESTfulService [GET /api/public/product/{}/images]", productId);
        return ResponseEntity.ok(productService.getProductImages(productId));
    }

    // ===========================================================
// PUBLIC: GET ALL PRODUCTS (optional pagination)
// ===========================================================
    @GetMapping("/list")
    public ResponseEntity<?> listProducts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) throws Exception {
        log.debug("Executing RESTfulService [GET /api/public/product/list]");
        return ResponseEntity.ok(productService.listProducts(offset, limit));
    }

    // ===========================================================
// PUBLIC: GET PRODUCTS BY CATEGORY
// ===========================================================
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) throws Exception {
        log.debug("Executing RESTfulService [GET /api/public/product/category/{}]", category);
        return ResponseEntity.ok(productService.getProductsByCategory(category, offset, limit));
    }

    // ===========================================================
// PUBLIC: SEARCH PRODUCTS BY NAME
// ===========================================================
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) throws Exception {
        log.debug("Executing RESTfulService [GET /api/public/product/search]");
        return ResponseEntity.ok(productService.searchProducts(name, offset, limit));
    }

}