package com.one.aim.service.impl;

import com.one.aim.bo.FileBO;
import com.one.aim.bo.ProductBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.helper.ProductHelper;
import com.one.aim.mapper.ProductMapper;
import com.one.aim.repo.ProductRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.rq.ProductRq;
import com.one.aim.rs.ProductRs;
import com.one.aim.rs.data.ProductDataRs;
import com.one.aim.rs.data.ProductDataRsList;
import com.one.aim.service.FileService;
import com.one.aim.service.ProductService;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final SellerRepo sellerRepo;
    private final FileService fileService;

    // ===========================================================
    // ADD PRODUCT (SELLER)
    // ===========================================================
    @Override
    @Transactional
    public BaseRs addProduct(ProductRq rq) {

        try {
            validateSellerAccess();   // KEEP AS IT IS

            List<String> errors = ProductHelper.validateProduct(rq);
            if (!errors.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
            }

            Long sellerId = AuthUtils.getLoggedUserId();
            SellerBO seller = sellerRepo.findById(sellerId).orElse(null);

            if (seller == null) {
                return ResponseUtils.failure(ErrorCodes.EC_SELLER_NOT_FOUND, "Seller not found");
            }

            ProductBO bo = new ProductBO();
            bo.setName(rq.getName());
            bo.setDescription(rq.getDescription());
            bo.setPrice(rq.getPrice());
            bo.setStock(rq.getStock());
            bo.setCategoryName(rq.getCategoryName());
            bo.setSeller(seller);
            bo.setSlug(generateUniqueSlug(rq.getName()));

            if (rq.getImages() != null) {
                for (MultipartFile file : rq.getImages()) {
                    if (!file.isEmpty()) {
                        FileBO uploaded = fileService.uploadAndReturnFile(file);
                        bo.getImageFileIds().add(uploaded.getId());
                    }
                }
            }

            productRepo.save(bo);

            ProductRs rs = ProductMapper.mapToProductRs(bo, fileService);
            return ResponseUtils.success(new ProductDataRs("Product created successfully", rs));
        }
        catch (RuntimeException ex) {

            return ResponseUtils.failure("EC_ADMIN_APPROVAL_REQUIRED", ex.getMessage());
        }
        catch (Exception e) {
            log.error("addProduct() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // UPDATE PRODUCT
    // ===========================================================
    @Override
    @Transactional
    public BaseRs updateProduct(ProductRq rq) {

        validateSellerAccess();  // REQUIRED

        try {
            if (rq.getDocId() == null) {
                return ResponseUtils.failure(ErrorCodes.EC_REQUIRED_DOCID, "Product ID required");
            }

            Long id = Long.parseLong(rq.getDocId());
            ProductBO bo = productRepo.findById(id).orElse(null);

            if (bo == null) {
                return ResponseUtils.failure(ErrorCodes.EC_PRODUCT_NOT_FOUND, "Product not found");
            }

            Long sellerId = AuthUtils.findLoggedInUser().getDocId();
            if (!bo.getSeller().getId().equals(sellerId)) {
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED, "You cannot edit another seller's product");
            }

            if (Utils.isNotEmpty(rq.getName())) bo.setName(rq.getName());
            if (Utils.isNotEmpty(rq.getDescription())) bo.setDescription(rq.getDescription());
            if (rq.getPrice() != null) bo.setPrice(rq.getPrice());
            if (rq.getStock() != null) bo.setStock(rq.getStock());
            if (Utils.isNotEmpty(rq.getCategoryName())) bo.setCategoryName(rq.getCategoryName());

            productRepo.save(bo);

            ProductRs rs = ProductMapper.mapToProductRs(bo, fileService);
            return ResponseUtils.success(new ProductDataRs("Product updated successfully", rs));
        }
        catch (Exception e) {
            log.error("updateProduct() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // UPLOAD IMAGES
    // ===========================================================
    @Override
    @Transactional
    public BaseRs uploadProductImages(Long productId, List<MultipartFile> files) {

        validateSellerAccess();  // REQUIRED

        try {
            ProductBO bo = productRepo.findById(productId).orElse(null);

            if (bo == null) {
                return ResponseUtils.failure(ErrorCodes.EC_PRODUCT_NOT_FOUND, "Product not found");
            }

            Long sellerId = AuthUtils.findLoggedInUser().getDocId();
            if (!bo.getSeller().getId().equals(sellerId)) {
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED, "Unauthorized image upload");
            }

            List<Long> imageIds = new ArrayList<>();

            for (MultipartFile f : files) {
                if (!f.isEmpty()) {
                    FileBO uploaded = fileService.uploadAndReturnFile(f);
                    bo.getImageFileIds().add(uploaded.getId());
                    imageIds.add(uploaded.getId());
                }
            }

            productRepo.save(bo);
            return ResponseUtils.success(new BaseDataRs("Images uploaded successfully", imageIds));
        }
        catch (Exception e) {
            log.error("uploadProductImages() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // GET PRODUCT IMAGES (SELLER ONLY)
    // ===========================================================
    @Override
    public BaseRs getProductImages(Long productId) {

        validateSellerAccess();  // REQUIRED

        try {
            if (productId == null || productId <= 0) {
                return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, "Invalid product ID");
            }

            ProductBO bo = productRepo.findById(productId).orElse(null);

            if (bo == null) {
                return ResponseUtils.failure(ErrorCodes.EC_PRODUCT_NOT_FOUND, "Product not found");
            }

            List<String> imageUrls = bo.getImageFileIds().stream()
                    .map(id -> "/api/files/public/" + id + "/view")
                    .toList();

            return ResponseUtils.success(
                    new BaseDataRs("Product images retrieved", imageUrls)
            );
        }
        catch (Exception e) {
            log.error("getProductImages() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // DELETE IMAGE
    // ===========================================================
    @Override
    @Transactional
    public BaseRs deleteProductImage(Long productId, Long imageId) {

        validateSellerAccess();  // REQUIRED

        try {
            ProductBO bo = productRepo.findById(productId).orElse(null);

            if (bo == null) {
                return ResponseUtils.failure(ErrorCodes.EC_PRODUCT_NOT_FOUND, "Product not found");
            }

            Long sellerId = AuthUtils.findLoggedInUser().getDocId();
            if (!bo.getSeller().getId().equals(sellerId)) {
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED, "Unauthorized action");
            }

            bo.getImageFileIds().remove(imageId);
            productRepo.save(bo);

            fileService.deleteFileById(String.valueOf(imageId));

            return ResponseUtils.success("Image deleted successfully");
        }
        catch (Exception e) {
            log.error("deleteProductImage() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // SHARE PRODUCT (PUBLIC)
    // ===========================================================
    @Override
    public String getShareableProduct(String slug) {

        try {
            ProductBO bo = productRepo.findBySlug(slug).orElse(null);

            if (bo == null) {
                return "Product not found";
            }

            return buildShareMessage(bo);
        }
        catch (Exception e) {
            log.error("getShareableProduct() failed", e);
            return "Error generating share message";
        }
    }


    // ===========================================================
    // DELETE PRODUCT
    // ===========================================================
    @Override
    @Transactional
    public BaseRs deleteProduct(Long productId) {

        validateSellerAccess();  // REQUIRED

        try {
            ProductBO bo = productRepo.findById(productId).orElse(null);

            if (bo == null) {
                return ResponseUtils.failure(ErrorCodes.EC_PRODUCT_NOT_FOUND, "Product not found");
            }

            Long sellerId = AuthUtils.getLoggedUserId();
            if (!bo.getSeller().getId().equals(sellerId)) {
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED, "Unauthorized action");
            }

            productRepo.delete(bo);

            return ResponseUtils.success("Product deleted successfully");
        }
        catch (Exception e) {
            log.error("deleteProduct() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // PUBLIC LISTING / SEARCH (NO SELLER VALIDATION)
    // ===========================================================
    @Override
    public BaseRs listProducts(int offset, int limit) {

        try {
            Pageable pageable = PageRequest.of(offset, limit);
            Page<ProductBO> page = productRepo.findAll(pageable);

            List<ProductRs> rsList = ProductMapper.mapToProductRsList(page.getContent(), fileService);
            return ResponseUtils.success(new ProductDataRsList("Products retrieved", rsList));
        }
        catch (Exception e) {
            log.error("listProducts() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    @Override
    public BaseRs getProductsByCategory(String category, int offset, int limit) {

        try {
            if (Utils.isEmpty(category)) {
                return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, "Category is required");
            }

            if (limit <= 0) limit = 20;
            if (offset < 0) offset = 0;

            Pageable pageable = PageRequest.of(offset / limit, limit);

            Page<ProductBO> page = productRepo.findByCategoryNameIgnoreCase(category, pageable);

            if (page.isEmpty()) {
                return ResponseUtils.failure(ErrorCodes.EC_NO_RECORDS_FOUND, "No products found for this category");
            }

            List<ProductRs> rsList =
                    ProductMapper.mapToProductRsList(page.getContent(), fileService);

            return ResponseUtils.success(
                    new ProductDataRsList("Products retrieved successfully", rsList)
            );
        }
        catch (Exception e) {
            log.error("getProductsByCategory() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    @Override
    public BaseRs searchProducts(String name, int offset, int limit) {

        try {
            Pageable pageable = PageRequest.of(offset, limit);
            Page<ProductBO> page = productRepo.findByNameContainingIgnoreCase(name, pageable);

            List<ProductRs> rsList = ProductMapper.mapToProductRsList(page.getContent(), fileService);
            return ResponseUtils.success(new ProductDataRsList("Products retrieved", rsList));
        }
        catch (Exception e) {
            log.error("searchProducts() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }


    // ===========================================================
    // HELPERS
    // ===========================================================
    private String generateSlug(String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateUniqueSlug(String name) {
        String slug = generateSlug(name);
        while (productRepo.existsBySlug(slug)) {
            slug = generateSlug(name);
        }
        return slug;
    }

    private String buildShareMessage(ProductBO product) {
        return "Check out this product: " + product.getName()
                + "\nhttp://localhost:8989/aimdev/api/public/product/" + product.getSlug();
    }

    private SellerBO validateSellerAccess() {

        Long id = AuthUtils.getLoggedUserId();
        String role = AuthUtils.getLoggedUserRole();

        if (!"SELLER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only seller can access this resource.");
        }

        SellerBO seller = sellerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (!seller.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before performing this action.");
        }

        if (!seller.isVerified()) {
            throw new RuntimeException("Your seller account is still pending admin approval.");
        }

        if (seller.isLocked()) {
            throw new RuntimeException("Your seller account has been locked by admin.");
        }

        return seller;
    }

}

