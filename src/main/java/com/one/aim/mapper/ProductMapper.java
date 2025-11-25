package com.one.aim.mapper;

import com.one.aim.bo.ProductBO;
import com.one.aim.rs.ProductRs;
import com.one.aim.service.FileService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ProductMapper {

    public static ProductRs mapToProductRs(ProductBO bo, FileService fileService) {

        if (bo == null) {
            log.warn("ProductBO is NULL");
            return null;
        }

        ProductRs rs = new ProductRs();

        // Product ID for frontend operations
        rs.setDocId(String.valueOf(bo.getId()));

        rs.setName(bo.getName());
        rs.setDescription(bo.getDescription());

        // Safe price
        rs.setPrice(bo.getPrice() == null ? 0.0 : bo.getPrice());

        // Safe stock
        rs.setStock(bo.getStock() == null ? 0 : bo.getStock());

        // Category (predefined or custom)
        rs.setCategoryName(bo.getCategoryName());
        rs.setCategoryId(bo.getCategoryId());

        // Slug
        rs.setSlug(bo.getSlug());

        // Default quantity
        rs.setQuantity(1);

        // Seller name
        if (bo.getSeller() != null) {
            rs.setSellerName(bo.getSeller().getFullName());
        }

        // Product images
        if (bo.getImageFileIds() != null && !bo.getImageFileIds().isEmpty()) {
            List<String> urls = bo.getImageFileIds().stream()
                    .map(id -> "/api/files/public/" + id + "/view")
                    .toList();
            rs.setImageUrls(urls);
        }

        // Share message will be constructed in service (NOT mapper)
        rs.setShareMessage(null);

        return rs;
    }


    public static List<ProductRs> mapToProductRsList(List<ProductBO> bos, FileService fileService) {
        if (bos == null || bos.isEmpty()) {
            return Collections.emptyList();
        }

        return bos.stream()
                .map(bo -> mapToProductRs(bo, fileService))
                .toList();
    }
}
