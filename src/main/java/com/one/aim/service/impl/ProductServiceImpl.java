package com.one.aim.service.impl;

import com.one.aim.bo.CategoryBO;
import com.one.aim.bo.FileBO;
import com.one.aim.bo.ProductBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.helper.ProductHelper;
import com.one.aim.mapper.ProductMapper;
import com.one.aim.repo.*;
import com.one.aim.rq.ProductRq;
import com.one.aim.rs.ProductRs;
import com.one.aim.rs.data.ProductDataRs;
import com.one.aim.rs.data.ProductDataRsList;
import com.one.aim.service.FileService;
import com.one.aim.service.ProductService;
import com.one.exception.BaseException;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.webjars.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final SellerRepo sellerRepo;
    private final FileService fileService;
    private final UserActivityService userActivityService;
    private final CategoryRepo  categoryRepo;
    private final OrderItemBORepo orderItemBORepo;
    private final ProductImageRepo productImageRepo;

    @Value("${app.frontend.product.url}")
    private String productFrontUrl;


    // ===========================================================
    // ADD PRODUCT (SELLER)
    // ===========================================================
    @Override
    @Transactional
    public BaseRs addProduct(ProductRq rq) {

        try {
            validateSellerAccess();

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
            bo.setSeller(seller);
            bo.setSlug(generateUniqueSlug(rq.getName()));

            // CATEGORY HANDLING
            if (rq.getCategoryId() != null) {

                CategoryBO category = categoryRepo.findById(rq.getCategoryId())
                        .orElse(null);

                if (category == null) {
                    return ResponseUtils.failure(ErrorCodes.EC_RECORD_NOT_FOUND, "Category not found");
                }

                bo.setCategoryId(category.getId());
                bo.setCategoryName(category.getName());
            }
            else {
                // CUSTOM CATEGORY
                if (Utils.isEmpty(rq.getCustomCategoryName())) {
                    return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, "Custom category name required");
                }

                bo.setCategoryId(null);
                bo.setCategoryName(rq.getCustomCategoryName());
            }

            // Upload images
            if (rq.getImages() != null && !rq.getImages().isEmpty()) {
                for (MultipartFile file : rq.getImages()) {
                    if (!file.isEmpty()) {
                        FileBO uploaded = fileService.uploadAndReturnFile(file);
                        bo.getImageFileIds().add(uploaded.getId());
                    }
                }
            }


            productRepo.save(bo);

            userActivityService.log(sellerId,"PRODUCT_CREATED","Created product: " + bo.getName());

            ProductRs rs = ProductMapper.mapToProductRs(bo, fileService);
            return ResponseUtils.success(new ProductDataRs("Product created successfully", rs));

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

        validateSellerAccess();

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
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED, "Unauthorized");
            }

            //  ALLOW ACTIVE-ONLY UPDATE
            if (rq.getActive() != null &&
                    rq.getName() == null &&
                    rq.getDescription() == null &&
                    rq.getPrice() == null &&
                    rq.getStock() == null &&
                    rq.getCategoryId() == null &&
                    rq.getCustomCategoryName() == null)
            {
                bo.setActive(rq.getActive());
                productRepo.save(bo);

                return ResponseUtils.success("Product status updated");
            }

            // NORMAL UPDATE (FULL FIELDS)
            if (Utils.isNotEmpty(rq.getName())) bo.setName(rq.getName());
            if (Utils.isNotEmpty(rq.getDescription())) bo.setDescription(rq.getDescription());
            if (rq.getPrice() != null) bo.setPrice(rq.getPrice());
            if (rq.getStock() != null) bo.setStock(rq.getStock());

            // Category
            if (rq.getCategoryId() != null) {
                CategoryBO category = categoryRepo.findById(rq.getCategoryId()).orElse(null);
                if (category == null) {
                    return ResponseUtils.failure(ErrorCodes.EC_RECORD_NOT_FOUND, "Category not found");
                }
                bo.setCategoryId(category.getId());
                bo.setCategoryName(category.getName());
            }

            // custom category
            if (Utils.isNotEmpty(rq.getCustomCategoryName())) {
                bo.setCategoryId(null);
                bo.setCategoryName(rq.getCustomCategoryName());
            }

            productRepo.save(bo);

            return ResponseUtils.success("Product updated");

        } catch (Exception e) {
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

        validateSellerAccess();

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

            // -----------------------------------------------------
            // USER ACTIVITY LOG — IMAGES UPLOADED
            // -----------------------------------------------------
            userActivityService.log(
                    sellerId,
                    "PRODUCT_IMAGE_UPLOADED",
                    "Uploaded images for product: " + bo.getName()
            );

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

        validateSellerAccess();

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

            // -----------------------------------------------------
            // USER ACTIVITY LOG — IMAGE DELETED
            // -----------------------------------------------------
            userActivityService.log(
                    sellerId,
                    "PRODUCT_IMAGE_DELETED",
                    "Deleted image from product: " + bo.getName()
            );

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

        validateSellerAccess();

        try {
            ProductBO bo = productRepo.findById(productId).orElse(null);

            if (bo == null) {
                return ResponseUtils.failure(ErrorCodes.EC_PRODUCT_NOT_FOUND, "Product not found");
            }

            Long sellerId = AuthUtils.getLoggedUserId();
            if (!bo.getSeller().getId().equals(sellerId)) {
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED, "Unauthorized action");
            }

            //  Instead of deleting — soft delete
            bo.setActive(false);
            productRepo.save(bo);

            userActivityService.log(
                    sellerId,
                    "PRODUCT_DELETED",
                    "Deleted product: " + bo.getName()
            );

            return ResponseUtils.success("Product deleted successfully");
        }
        catch (Exception e) {
            log.error("deleteProduct() failed", e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR, e.getMessage());
        }
    }




    @Override
    public BaseRs listProducts(int offset, int limit) {

        try {
            if (limit <= 0) limit = 20;
            if (offset < 0) offset = 0;

            int page = offset / limit;

            Pageable pageable = PageRequest.of(page, limit);

            // ONLY ACTIVE PRODUCTS
            Page<ProductBO> pageData = productRepo.findByActiveTrue(pageable);

            List<ProductRs> rsList =
                    ProductMapper.mapToProductRsList(pageData.getContent(), fileService);

            return ResponseUtils.success(
                    new ProductDataRsList("Products retrieved successfully", rsList)
            );

        } catch (Exception e) {
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

    @Override
    public BaseRs listSellerProducts(boolean showInactive) {
        Long sellerId = AuthUtils.findLoggedInUser().getDocId();

        List<ProductBO> products;

        if (showInactive) {
            // return all (active + inactive)
            products = productRepo.findBySellerId(sellerId);
        } else {
            // return only active
            products = productRepo.findBySellerId(sellerId)
                    .stream()
                    .filter(ProductBO::isActive)
                    .collect(Collectors.toList());
        }

        for (ProductBO p : products) {
            Integer sold = orderItemBORepo.countProductSales(p.getId());
            p.setSoldItem(sold == null ? 0 : sold);
        }

        return ResponseUtils.success(products);
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
                + "\n" + productFrontUrl + product.getSlug();
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your seller account is still pending admin approval.");

        }

        if (seller.isLocked()) {
            throw new RuntimeException("Your seller account has been locked by admin.");
        }

        return seller;
    }

}

