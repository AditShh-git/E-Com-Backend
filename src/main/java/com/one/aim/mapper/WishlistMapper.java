package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.one.aim.bo.CartBO;
import com.one.aim.bo.ProductBO;
import com.one.aim.rs.WishlistRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WishlistMapper {

    public static WishlistRs mapToWishlistRs(ProductBO product) {

        WishlistRs rs = new WishlistRs();

        rs.setProductId(String.valueOf(product.getId()));
        rs.setName(product.getName());
        rs.setDescription(product.getDescription());
        rs.setPrice(product.getPrice());
        rs.setCategory(product.getCategoryName());

        if (product.getImageFileIds() != null && !product.getImageFileIds().isEmpty()) {
            rs.setImageId(product.getImageFileIds().get(0)); // first image
        }

        return rs;
    }

    public static List<WishlistRs> mapToWishlistRsList(List<ProductBO> products) {
        return products.stream()
                .map(WishlistMapper::mapToWishlistRs)
                .toList();
    }

}
