package com.one.aim.controller;

import com.one.aim.rq.ProductRq;
import com.one.aim.service.ProductService;
import com.one.vm.core.BaseRs;
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
    public ResponseEntity<BaseRs> addProduct(@ModelAttribute ProductRq rq) throws Exception {
        BaseRs response = productService.addProduct(rq);
        return ResponseEntity.ok(response);
    }



    // ===========================================================
    // UPDATE PRODUCT (SELLER ONLY)
    // ===========================================================
    @PreAuthorize("hasAuthority('SELLER')")
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseRs> updateProduct(@ModelAttribute ProductRq rq) throws Exception {
        log.debug("Executing RESTfulService [PUT /api/seller/product/update]");
        BaseRs response = productService.updateProduct(rq);
        return ResponseEntity.ok(response);
    }

    // ===========================================================
    // UPLOAD MULTIPLE IMAGES (SELLER ONLY)
    // ===========================================================
    @PreAuthorize("hasAuthority('SELLER')")
    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseRs> uploadProductImages(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files) throws Exception {

        log.debug("Executing RESTfulService [POST /api/seller/product/{}/images]", productId);
        BaseRs response = productService.uploadProductImages(productId, files);
        return ResponseEntity.ok(response);
    }

    // ===========================================================
    // DELETE PRODUCT IMAGE (SELLER OR ADMIN)
    // ===========================================================
    @PreAuthorize("hasAnyAuthority('SELLER', 'ADMIN')")
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<BaseRs> deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) throws Exception {

        log.debug("Executing RESTfulService [DELETE /api/seller/product/{}/images/{}]", productId, imageId);
        BaseRs response = productService.deleteProductImage(productId, imageId);
        return ResponseEntity.ok(response);
    }

    // ===========================================================
    // DELETE PRODUCT (SELLER OR ADMIN)
    // ===========================================================
    @PreAuthorize("hasAnyAuthority('SELLER', 'ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<BaseRs> deleteProduct(@PathVariable Long productId) throws Exception {
        log.debug("Executing RESTfulService [DELETE /api/seller/product/{}]", productId);
        BaseRs response = productService.deleteProduct(productId);
        return ResponseEntity.ok(response);
    }

    // ===========================================================
// LIST PRODUCTS (SELLER ONLY)
// ===========================================================
//    @PreAuthorize("hasAuthority('SELLER')")
//    @GetMapping("/list")
//    public ResponseEntity<BaseRs> listMyProducts() throws Exception {
//        log.debug("Executing RESTfulService [GET /api/seller/product/list]");
//        BaseRs response = productService.listSellerProducts();
//        return ResponseEntity.ok(response);
//    }

    @PreAuthorize("hasAuthority('SELLER')")
    @GetMapping("/list")
    public ResponseEntity<BaseRs> listMyProducts(
            @RequestParam(defaultValue = "false") boolean showInactive
    ) throws Exception {
        log.debug("Executing RESTfulService [GET /api/seller/product/list]");
        BaseRs response = productService.listSellerProducts(showInactive);
        return ResponseEntity.ok(response);
    }


}
