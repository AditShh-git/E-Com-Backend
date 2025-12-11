package com.one.aim.mapper;

import com.one.aim.bo.ProductBO;
import com.one.aim.rs.ProductCardRs;
import com.one.aim.rs.ProductRs;
import com.one.aim.service.FileService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ProductMapper {


    // ================================================
    // HOMEPAGE PRODUCT CARD MAPPER (Public UI)
    // ================================================
    public static ProductCardRs toCardRs(ProductBO bo, FileService fileService) {

        if (bo == null) return null;

        ProductCardRs rs = new ProductCardRs();

        rs.setDocId(String.valueOf(bo.getId()));
        rs.setName(bo.getName());
        rs.setSlug(bo.getSlug());
        rs.setPrice(bo.getPrice());
        rs.setCategoryName(bo.getCategoryName());
        rs.setCategoryId(bo.getCategoryId());

        if (bo.getImageFileIds() != null && !bo.getImageFileIds().isEmpty()) {
            Long fileId = bo.getImageFileIds().get(0);
            rs.setImage("/api/files/public/" + fileId + "/view");
        }

        int stock = bo.getStock() == null ? 0 : bo.getStock();
        rs.setInStock(stock > 0);

        rs.setAverageRating(4.5);
        rs.setReviewCount(20L);

        return rs;
    }




    // ================================================
    // EXISTING METHODS (KEEP AS IS)
    // ================================================
    public static ProductRs mapToProductRs(ProductBO bo, FileService fileService) {
        if (bo == null) {
            log.warn("ProductBO is NULL");
            return null;
        }

        ProductRs rs = new ProductRs();

        rs.setDocId(String.valueOf(bo.getId()));
        rs.setName(bo.getName());
        rs.setDescription(bo.getDescription());
        rs.setPrice(bo.getPrice() == null ? 0.0 : bo.getPrice());
        rs.setStock(bo.getStock() == null ? 0 : bo.getStock());
        rs.setCategoryName(bo.getCategoryName());
        rs.setCategoryId(bo.getCategoryId());
        rs.setSlug(bo.getSlug());
        rs.setQuantity(1);

        if (bo.getSeller() != null) {
            rs.setSellerName(bo.getSeller().getFullName());
        }

        if (bo.getImageFileIds() != null && !bo.getImageFileIds().isEmpty()) {
            List<String> urls = bo.getImageFileIds().stream()
                    .map(id -> "/api/files/public/" + id + "/view")
                    .toList();
            rs.setImageUrls(urls);
        }

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
