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
        rs.setPrice(bo.getPrice());
        rs.setStock(bo.getStock());
        rs.setCategoryName(bo.getCategoryName());
        rs.setSlug(bo.getSlug());
        rs.setSellerName(bo.getSeller() != null ? bo.getSeller().getFullName() : null);

        if (bo.getImageFileIds() != null && !bo.getImageFileIds().isEmpty()) {
            List<String> urls = bo.getImageFileIds().stream()
                    .map(id -> "/api/files/" + id + "/view")
                    .toList();
            rs.setImageUrls(urls);
        }

        // Optionally add share link (if you want automatic)
        rs.setShareMessage("🛒 Check out this on ShopEase: " + rs.getName() +
                " — " + rs.getDescription() + "\n👉 " +
                "http://localhost:8989/aimdev/api/public/product/" + rs.getSlug());

        return rs;
    }


    public static List<ProductRs> mapToProductRsList(List<ProductBO> bos, FileService fileService) {
        if (bos == null || bos.isEmpty()) {
            log.warn("ProductBO list is empty");
            return Collections.emptyList();
        }

        return bos.stream()
                .map(bo -> mapToProductRs(bo, fileService))
                .toList();
    }
}
