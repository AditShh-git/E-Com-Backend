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
import org.springframework.data.domain.Sort;
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
    private final CartRepo  cartRepo;

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
                return ResponseUtils.failure(
                        ErrorCodes.EC_INVALID_INPUT,
                        errors
                );
            }

            Long sellerId = AuthUtils.getLoggedUserId();
            SellerBO seller = sellerRepo.findById(sellerId)
                    .orElseThrow(() -> new RuntimeException("Seller not found"));

            //  Validate stock must be >= 1
            if (rq.getStock() == null || rq.getStock() < 1) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_INVALID_INPUT,
                        "Stock must be at least 1"
                );
            }

            //  Validate price > 0
            if (rq.getPrice() == null || rq.getPrice() <= 0) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_INVALID_INPUT,
                        "Price must be greater than 0"
                );
            }

            ProductBO bo = new ProductBO();
            bo.setName(rq.getName());
            bo.setDescription(rq.getDescription());
            bo.setPrice(rq.getPrice());
            bo.setStock(rq.getStock());
            bo.setSeller(seller);
            bo.setSlug(generateUniqueSlug(rq.getName()));
            bo.updateLowStock();

            // CATEGORY HANDLING
            if (rq.getCategoryId() != null) {
                CategoryBO category = categoryRepo.findById(rq.getCategoryId())
                        .orElse(null);

                if (category == null) {
                    return ResponseUtils.failure(
                            ErrorCodes.EC_RECORD_NOT_FOUND,
                            "Category not found"
                    );
                }

                bo.setCategoryId(category.getId());
                bo.setCategoryName(category.getName());
            } else {
                if (Utils.isEmpty(rq.getCustomCategoryName())) {
                    return ResponseUtils.failure(
                            ErrorCodes.EC_INVALID_INPUT,
                            "Custom category name required"
                    );
                }
                bo.setCategoryId(null);
                bo.setCategoryName(rq.getCustomCategoryName());
            }

            // =====================================================
            // IMAGE VALIDATION & UPLOAD
            // =====================================================
            List<MultipartFile> images = rq.getImages();
            if (images != null && !images.isEmpty()) {

                //  Max 5 images allowed
                if (images.size() > 5) {
                    return ResponseUtils.failure(
                            "TOO_MANY_IMAGES",
                            "Maximum 5 images allowed"
                    );
                }

                for (MultipartFile file : images) {
                    if (file.isEmpty()) continue;

                    //  Accept only JPG/PNG
                    String type = file.getContentType();
                    if (!List.of("image/jpeg", "image/png")
                            .contains(type)) {
                        return ResponseUtils.failure(
                                "INVALID_FILE_TYPE",
                                "Only JPG and PNG images are allowed"
                        );
                    }

                    //  Max 2MB per file
                    if (file.getSize() > 2 * 1024 * 1024) {
                        return ResponseUtils.failure(
                                "FILE_TOO_LARGE",
                                "File size must be <= 2MB"
                        );
                    }

                    FileBO uploaded = fileService.uploadAndReturnFile(file);
                    bo.getImageFileIds().add(uploaded.getId());
                }
            }

            //  Prevent product creation without image
            if (bo.getImageFileIds().isEmpty()) {
                return ResponseUtils.failure(
                        "NO_IMAGE",
                        "Product must have at least one image"
                );
            }

            productRepo.save(bo);

            userActivityService.log(
                    sellerId,
                    "PRODUCT_CREATED",
                    "Created product: " + bo.getName()
            );

            ProductRs rs = ProductMapper.mapToProductRs(bo, fileService);
            return ResponseUtils.success(
                    new ProductDataRs("Product created successfully", rs)
            );

        } catch (Exception e) {
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

        try {
            validateSellerAccess();

            if (rq.getDocId() == null) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_REQUIRED_DOCID,
                        "Product ID required"
                );
            }

            Long productId = Long.parseLong(rq.getDocId());
            ProductBO product = productRepo.findById(productId)
                    .orElse(null);

            if (product == null) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_PRODUCT_NOT_FOUND,
                        "Product not found"
                );
            }

            Long sellerId = AuthUtils.findLoggedInUser().getDocId();
            if (!product.getSeller().getId().equals(sellerId)) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_UNAUTHORIZED,
                        "Unauthorized"
                );
            }

            // =====================================================================
            // ONLY ACTIVE/INACTIVE TOGGLE
            // =====================================================================
            if (rq.getActive() != null &&
                    rq.getName() == null &&
                    rq.getDescription() == null &&
                    rq.getPrice() == null &&
                    rq.getStock() == null &&
                    rq.getCategoryId() == null &&
                    rq.getCustomCategoryName() == null)
            {
                product.setActive(rq.getActive());
                productRepo.save(product);

                //  BONUS #1: Remove from all carts if deactivated
                if (!product.isActive()) {
                    cartRepo.deleteByProduct_Id(product.getId());
                }

                ProductRs rs = ProductMapper.mapToProductRs(product, fileService);
                return ResponseUtils.success(
                        new ProductDataRs("Product status updated", rs)
                );
            }

            // =====================================================================
            // FULL PRODUCT UPDATE
            // =====================================================================

            // Name
            if (Utils.isNotEmpty(rq.getName())) {
                product.setName(rq.getName());
                product.setSlug(generateUniqueSlug(rq.getName()));
            }

            // Description
            if (Utils.isNotEmpty(rq.getDescription())) {
                product.setDescription(rq.getDescription());
            }

            // Price validation
            if (rq.getPrice() != null) {
                if (rq.getPrice() <= 0) {
                    return ResponseUtils.failure(
                            ErrorCodes.EC_INVALID_INPUT,
                            "Price must be greater than 0"
                    );
                }
                product.setPrice(rq.getPrice());
            }

            boolean stockChanged = false;

            // Stock validation
            if (rq.getStock() != null) {
                if (rq.getStock() < 0) {
                    return ResponseUtils.failure(
                            ErrorCodes.EC_INVALID_INPUT,
                            "Stock cannot be negative"
                    );
                }
                product.setStock(rq.getStock());
                product.updateLowStock();
                stockChanged = true;
            }

            // CATEGORY HANDLING
            if (rq.getCategoryId() != null) {
                CategoryBO category =
                        categoryRepo.findById(rq.getCategoryId()).orElse(null);
                if (category == null) {
                    return ResponseUtils.failure(
                            ErrorCodes.EC_RECORD_NOT_FOUND,
                            "Category not found"
                    );
                }
                product.setCategoryId(category.getId());
                product.setCategoryName(category.getName());
            }
            else if (Utils.isNotEmpty(rq.getCustomCategoryName())) {
                product.setCategoryId(null);
                product.setCategoryName(rq.getCustomCategoryName());
            }

            productRepo.save(product);

            // =====================================================
// IMAGE ADDING (NEW)
// =====================================================
            if (rq.getImages() != null && !rq.getImages().isEmpty()) {

                int currentImages = product.getImageFileIds().size();
                int newImages = rq.getImages().size();

                if (currentImages + newImages > 5) {
                    return ResponseUtils.failure(
                            "TOO_MANY_IMAGES",
                            "You can only upload up to 5 images"
                    );
                }

                for (MultipartFile file : rq.getImages()) {
                    if (file.isEmpty()) continue;

                    String contentType = file.getContentType();
                    if (!List.of("image/jpeg", "image/png").contains(contentType)) {
                        return ResponseUtils.failure(
                                "INVALID_FILE_TYPE",
                                "Only JPG and PNG allowed"
                        );
                    }

                    if (file.getSize() > 2 * 1024 * 1024) {
                        return ResponseUtils.failure(
                                "FILE_TOO_LARGE",
                                "Max size 2MB allowed"
                        );
                    }

                    FileBO uploaded = fileService.uploadAndReturnFile(file);
                    product.getImageFileIds().add(uploaded.getId());
                }
            }

            productRepo.save(product);

            // =====================================================================
            //  BONUS #2: Remove cart entries if stock reduced or became zero
            // =====================================================================
            if (stockChanged) {
                if (product.getStock() <= 0) {
                    cartRepo.deleteByProduct_Id(product.getId());
                } else {
                    // If qty in any cart > new stock → disable those cart items
                    cartRepo.findAllByProduct_Id(product.getId()).stream()
                            .filter(c -> c.getQuantity() > product.getStock())
                            .forEach(c -> {
                                c.setEnabled(false);
                                cartRepo.save(c);
                            });
                }
            }

            // =====================================================================
            // RESPONSE WITH UPDATED PRODUCT DATA
            // =====================================================================
            ProductRs rs = ProductMapper.mapToProductRs(product, fileService);
            return ResponseUtils.success(
                    new ProductDataRs("Product updated successfully", rs)
            );
        }
        catch (Exception e) {
            log.error("updateProduct() failed", e);
            return ResponseUtils.failure(
                    ErrorCodes.EC_INTERNAL_ERROR,
                    e.getMessage()
            );
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

        try {
            Long sellerId = AuthUtils.findLoggedInUser().getDocId();

            ProductBO product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            //  Ownership check (Security)
            if (!product.getSeller().getId().equals(sellerId)) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_UNAUTHORIZED,
                        "You are not allowed to modify this product"
                );
            }

            List<Long> imageList = product.getImageFileIds();
            if (imageList == null || imageList.isEmpty()) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_IMAGE_NOT_FOUND,
                        "No images found for this product"
                );
            }

            //  Prevent removing last image
            if (imageList.size() == 1) {
                return ResponseUtils.failure(
                        "LAST_IMAGE",
                        "At least one image must remain for this product"
                );
            }

            //  Ensure image belongs to this product
            if (!imageList.contains(imageId)) {
                return ResponseUtils.failure(
                        ErrorCodes.EC_IMAGE_NOT_FOUND,
                        "Image does not belong to this product"
                );
            }

            //  Remove image reference from product
            imageList.remove(imageId);
            productRepo.save(product);

            //  Delete actual file
            fileService.deleteFileById(String.valueOf(imageId));

            // Log action
            userActivityService.log(
                    sellerId,
                    "PRODUCT_IMAGE_DELETED",
                    "Deleted image " + imageId + " from product: " + product.getName()
            );

            return ResponseUtils.success("Image deleted successfully");
        }
        catch (Exception e) {
            log.error("deleteProductImage() failed", e);
            return ResponseUtils.failure(
                    ErrorCodes.EC_INTERNAL_ERROR,
                    "Failed to delete product image"
            );
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

    @Override
    public BaseRs listAdminProducts(int page, int size, String sortBy, String direction) throws Exception {

        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductBO> productsPage = productRepo.findAll(pageable);

        List<Map<String, Object>> result = productsPage.getContent()
                .stream()
                .map(p -> {
                    Map<String, Object> map = new LinkedHashMap<>();

                    map.put("productId", p.getId());
                    map.put("name", p.getName());
                    map.put("description", p.getDescription());
                    map.put("price", p.getPrice());
                    map.put("stock", p.getStock());
                    map.put("categoryName", p.getCategoryName());
                    map.put("categoryId", p.getCategoryId());
                    map.put("slug", p.getSlug());
                    map.put("createdAt", p.getCreatedAt());
                    map.put("updatedAt", p.getUpdatedAt());

                    // Image URLs
                    List<String> imageUrls = p.getImageFileIds()
                            .stream()
                            .map(id -> "/api/files/public/" + id + "/view")
                            .toList();
                    map.put("imageUrls", imageUrls);

                    // Seller info
                    if (p.getSeller() != null) {
                        SellerBO s = p.getSeller();
                        map.put("sellerId", s.getSellerId());
                        map.put("sellerName", s.getFullName());
                        map.put("sellerEmail", s.getEmail());
                        map.put("sellerVerified", s.isVerified());
                    }

                    return map;
                })
                .toList();

        // Build response body
        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("products", result);
        responseData.put("page", productsPage.getNumber());
        responseData.put("size", productsPage.getSize());
        responseData.put("totalElements", productsPage.getTotalElements());
        responseData.put("totalPages", productsPage.getTotalPages());
        responseData.put("isLast", productsPage.isLast());
        responseData.put("isFirst", productsPage.isFirst());

        // Build BaseDataRs
        BaseDataRs dataRs = new BaseDataRs();
        dataRs.setMessage("Admin product list fetched");
        dataRs.setData(responseData);

        // Build BaseRs
        BaseRs response = new BaseRs();
        response.setStatus("SUCCESS");
        response.setData(dataRs);

        return response;
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

