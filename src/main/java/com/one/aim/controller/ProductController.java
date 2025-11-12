package com.one.aim.controller;

import com.one.aim.rq.ProductRq;
import com.one.aim.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/seller/product")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ===========================================================
    // ADD PRODUCT (SELLER ONLY)
    // ===========================================================
    @PreAuthorize("hasAuthority('SELLER')")
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(@ModelAttribute ProductRq rq) throws Exception {
        log.debug("Executing RESTfulService [POST /api/seller/product/add]");
        return ResponseEntity.ok(productService.addProduct(rq));
    }

    // ===========================================================
    // UPDATE PRODUCT (SELLER ONLY)
    // ===========================================================
    @PreAuthorize("hasAuthority('SELLER')")
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(@ModelAttribute ProductRq rq) throws Exception {
        log.debug("Executing RESTfulService [PUT /api/seller/product/update]");
        return ResponseEntity.ok(productService.updateProduct(rq));
    }

    // ===========================================================
    // UPLOAD MULTIPLE IMAGES (SELLER ONLY)
    // ===========================================================
    @PreAuthorize("hasAuthority('SELLER')")
    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProductImages(@PathVariable Long productId,
                                                 @RequestParam("files") List<MultipartFile> files) throws Exception {
        log.debug("Executing RESTfulService [POST /api/seller/product/{}/images]", productId);
        return ResponseEntity.ok(productService.uploadProductImages(productId, files));
    }

    // ===========================================================
    // DELETE PRODUCT IMAGE (SELLER OR ADMIN)
    // ===========================================================
    @PreAuthorize("hasAnyAuthority('SELLER', 'ADMIN')")
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long productId,
                                                @PathVariable Long imageId) throws Exception {
        log.debug("Executing RESTfulService [DELETE /api/seller/product/{}/images/{}]", productId, imageId);
        return ResponseEntity.ok(productService.deleteProductImage(productId, imageId));
    }

    // ===========================================================
    // DELETE PRODUCT (SELLER OR ADMIN)
    // ===========================================================
    @PreAuthorize("hasAnyAuthority('SELLER', 'ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) throws Exception {
        log.debug("Executing RESTfulService [DELETE /api/seller/product/{}]", productId);
        return ResponseEntity.ok(productService.deleteProduct(productId));
    }
}
