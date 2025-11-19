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

        rs.setDocId(String.valueOf(bo.getId()));
        rs.setName(bo.getName());
        rs.setDescription(bo.getDescription());

        //  FIX: Always return price safely
        rs.setPrice(bo.getPrice() == null ? 0.0 : bo.getPrice());

        //  FIX: make stock safe (0 if null)
        rs.setStock(bo.getStock() == null ? 0 : bo.getStock());

        rs.setCategoryName(bo.getCategoryName());
        rs.setSlug(bo.getSlug());

        //  NEW: Most e-commerce sites show "default quantity = 1"
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

        // Shareable link
        String shareLink = "http://localhost:8989/aimdev/api/public/product/" + bo.getSlug();
        rs.setShareMessage("Check out this product: " + bo.getName() + "\n" + shareLink);

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
