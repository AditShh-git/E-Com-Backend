package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.one.aim.bo.CartBO;
import com.one.aim.bo.ProductBO;
import com.one.aim.rs.WishlistRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

public class WishlistMapper {

    // Single product mapping
    public static WishlistRs mapToWishlistRs(ProductBO product) {

        if (product == null) return null;

        String firstImageUrl = null;

        if (product.getImageFileIds() != null && !product.getImageFileIds().isEmpty()) {
            Long imageId = product.getImageFileIds().get(0);
            firstImageUrl = "/api/files/public/" + imageId + "/view";
        }

        return WishlistRs.builder()
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .inStock(product.getStock() != null && product.getStock() > 0)
                .lowStock(product.isLowStock())
                .productImageUrl(firstImageUrl)
                .categoryName(product.getCategoryName())
                .build();
    }


    // List mapping
    public static List<WishlistRs> mapToWishlistRsList(List<ProductBO> products) {
        if (products == null || products.isEmpty()) return List.of();

        return products.stream()
                .map(WishlistMapper::mapToWishlistRs)
                .toList();
    }
}

