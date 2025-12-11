package com.one.aim.controller;

import com.one.aim.rs.ProductCardRs;
import com.one.aim.rs.ProductRs;
import com.one.aim.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/product")
@RequiredArgsConstructor
@Slf4j
public class PublicProductController {

    private final ProductService productService;

    // ===========================================================
    // PUBLIC: VIEW PRODUCT DETAILS BY SLUG
    // ===========================================================
    @GetMapping("/{slug}")
    public ResponseEntity<?> getShareProduct(@PathVariable String slug) throws Exception {
        String shareMessage = productService.getShareableProduct(slug);

        Map<String, Object> response = new HashMap<>();
        response.put("shareMessage", shareMessage);
        return ResponseEntity.ok(response);
    }

    // ===========================================================
    // PUBLIC: GET PRODUCT IMAGES
    // ===========================================================
    @GetMapping("/{productId}/images")
    public ResponseEntity<?> getProductImages(@PathVariable Long productId) throws Exception {
        log.debug("Executing RESTfulService [GET /api/public/product/{}/images]", productId);
        return ResponseEntity.ok(productService.getProductImages(productId));
    }

    // ===========================================================
    // PUBLIC: LIST PRODUCTS WITH FILTERS + PAGINATION
    // ===========================================================
    @GetMapping
    public Page<ProductCardRs> getProducts(
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) throws Exception {
        log.debug("Executing RESTfulService [GET /api/public/product]");
        return productService.getProducts(category, page, size, sort);
    }

    // ===========================================================
    // PUBLIC: SEARCH PRODUCTS BY NAME + CATEGORY FILTER
    // ===========================================================
    @GetMapping("/search")
    public Page<ProductCardRs> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws Exception {
        log.debug("Executing RESTfulService [GET /api/public/product/search]");
        return productService.searchProducts(q, category, page, size);
    }

}
