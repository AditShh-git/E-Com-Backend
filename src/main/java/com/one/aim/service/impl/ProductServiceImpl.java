package com.one.aim.service.impl;

import com.one.aim.bo.FileBO;
import com.one.aim.bo.ProductBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.helper.ProductHelper;
import com.one.aim.mapper.ProductMapper;
import com.one.aim.repo.ProductRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.rq.ProductRq;
import com.one.aim.rs.ProductRs;
import com.one.aim.rs.data.ProductDataRs;
import com.one.aim.service.FileService;
import com.one.aim.service.ProductService;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final SellerRepo sellerRepo;
    private final FileService fileService;

    // ===========================================================
    // ADD PRODUCT
    // ===========================================================
    @Override
    @Transactional
    public BaseRs addProduct(ProductRq rq) throws Exception {

        log.debug("Executing addProduct(ProductRq) -> {}", rq.getName());

        // Validate product request
        List<String> errors = ProductHelper.validateProduct(rq);
        if (!errors.isEmpty()) {
            return ResponseUtils.failure("EC_INVALID_INPUT", errors);
        }

        Long sellerId = AuthUtils.findLoggedInUser().getDocId();
        SellerBO seller = sellerRepo.findById(sellerId)
                .orElseThrow(() -> new IllegalStateException("Seller not found"));

        // Prepare new ProductBO
        ProductBO bo = new ProductBO();
        bo.setName(rq.getName());
        bo.setDescription(rq.getDescription());
        bo.setPrice(rq.getPrice());
        bo.setStock(rq.getStock());
        bo.setCategoryName(rq.getCategoryName());
        bo.setSeller(seller);

        // ✅ Generate SEO-friendly slug
        String slug = generateUniqueSlug(rq.getName());
        bo.setSlug(slug);

        // ✅ Handle image uploads
        if (rq.getImages() != null && !rq.getImages().isEmpty()) {
            for (MultipartFile file : rq.getImages()) {
                FileBO uploaded = fileService.uploadAndReturnFile(file);
                bo.getImageFileIds().add(uploaded.getId());
            }
        }

        productRepo.save(bo);

        ProductRs rs = ProductMapper.mapToProductRs(bo, fileService);
        return ResponseUtils.success(new ProductDataRs("Product created successfully", rs));
    }

    // ===========================================================
    // UPDATE PRODUCT
    // ===========================================================
    @Override
    @Transactional
    public BaseRs updateProduct(ProductRq rq) throws Exception {

        log.debug("Executing updateProduct(ProductRq) -> {}", rq.getDocId());

        if (rq.getDocId() == null) {
            return ResponseUtils.failure("EC_REQUIRED_DOCID", "Product ID is required for update");
        }

        Long productId = Long.parseLong(rq.getDocId());
        ProductBO bo = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!bo.getSeller().getId().equals(AuthUtils.findLoggedInUser().getDocId())) {
            return ResponseUtils.failure("EC_UNAUTHORIZED", "Cannot edit another seller’s product");
        }

        // ✅ Update editable fields
        if (Utils.isNotEmpty(rq.getName())) bo.setName(rq.getName());
        if (Utils.isNotEmpty(rq.getDescription())) bo.setDescription(rq.getDescription());
        if (rq.getPrice() != null) bo.setPrice(rq.getPrice());
        if (rq.getStock() != null) bo.setStock(rq.getStock());
        if (Utils.isNotEmpty(rq.getCategoryName())) bo.setCategoryName(rq.getCategoryName());

        productRepo.save(bo);

        ProductRs rs = ProductMapper.mapToProductRs(bo, fileService);
        return ResponseUtils.success(new ProductDataRs("Product updated successfully", rs));
    }

    // ===========================================================
    // UPLOAD MULTIPLE IMAGES
    // ===========================================================
    @Override
    @Transactional
    public BaseRs uploadProductImages(Long productId, List<MultipartFile> files) throws Exception {

        ProductBO bo = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!bo.getSeller().getId().equals(AuthUtils.findLoggedInUser().getDocId())) {
            return ResponseUtils.failure("EC_UNAUTHORIZED", "You can only upload images for your own products.");
        }

        List<Long> newImageIds = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                FileBO uploaded = fileService.uploadAndReturnFile(file);
                bo.getImageFileIds().add(uploaded.getId());
                newImageIds.add(uploaded.getId());
            }
        }

        productRepo.save(bo);

        return ResponseUtils.success(new BaseDataRs("Images uploaded successfully", newImageIds));
    }

    // ===========================================================
    // GET PRODUCT IMAGES
    // ===========================================================
    @Override
    public BaseRs getProductImages(Long productId) throws Exception {

        ProductBO bo = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        List<String> imageUrls = bo.getImageFileIds().stream()
                .map(id -> "/api/files/" + id + "/view")
                .toList();

        return ResponseUtils.success(new BaseDataRs("Product images retrieved", imageUrls));
    }

    // ===========================================================
    // DELETE PRODUCT IMAGE
    // ===========================================================
    @Override
    @Transactional
    public BaseRs deleteProductImage(Long productId, Long imageId) throws Exception {

        ProductBO bo = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!bo.getSeller().getId().equals(AuthUtils.findLoggedInUser().getDocId())) {
            return ResponseUtils.failure("EC_UNAUTHORIZED", "Cannot delete image of another seller’s product.");
        }

        bo.getImageFileIds().remove(imageId);
        productRepo.save(bo);

        fileService.deleteFileById(String.valueOf(imageId));

        return ResponseUtils.success("Image deleted successfully");
    }

    // ===========================================================
    // SHARE PRODUCT (PUBLIC)
    // ===========================================================
    @Override
    public ProductRs getShareableProduct(String slug) throws Exception {

        ProductBO product = productRepo.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Product not found with slug: " + slug));

        ProductRs rs = ProductMapper.mapToProductRs(product, fileService);
        rs.setShareMessage(buildShareMessage(product)); // include share link message
        return rs;
    }

    // ===========================================================
    // DELETE PRODUCT
    // ===========================================================
    @Override
    public BaseRs deleteProduct(Long productId) throws Exception {

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }

        ProductBO product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        productRepo.delete(product);

        log.info("✅ Product deleted successfully with ID: {}", productId);
        return ResponseUtils.success("Product deleted successfully");
    }

    // ===========================================================
    // PRIVATE HELPERS
    // ===========================================================

    private String generateSlug(String name) {
        if (name == null || name.isBlank()) {
            return UUID.randomUUID().toString();
        }
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        return baseSlug + "-" + uniqueSuffix;
    }

    private String generateUniqueSlug(String name) {
        String slug = generateSlug(name);
        while (productRepo.existsBySlug(slug)) {
            slug = generateSlug(name);
        }
        return slug;
    }

    private String buildShareMessage(ProductBO product) {
        String baseUrl = "http://localhost:8989/aimdev/api/public/product/" + product.getSlug();
        return "🛒 Check out this on ShopEase: " + product.getName() +
                " — " + product.getDescription() + "\n" +
                "👉 " + baseUrl;
    }
}
