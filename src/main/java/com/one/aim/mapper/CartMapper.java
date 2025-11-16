package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.one.aim.bo.CartBO;
import com.one.aim.bo.ProductBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.VendorBO;
import com.one.aim.rs.CartMaxRs;
import com.one.aim.rs.CartRs;
import com.one.aim.service.FileService;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CartMapper {

    public static CartRs mapToCartRs(CartBO bo, FileService fileService) {

        if (bo == null) return null;

        CartRs rs = new CartRs();

        // ===============================
        // Basic Cart Fields
        // ===============================
        rs.setId(bo.getId());
        rs.setPname(bo.getPname());
        rs.setPrice(bo.getPrice());
        rs.setQuantity(bo.getQuantity());

        // ===============================
        // Product Snapshot
        // ===============================
        ProductBO product = bo.getProduct();

        if (product != null) {

            if (product.getDescription() != null) {
                rs.setDescription(product.getDescription());
            }

            if (product.getCategoryName() != null) {
                rs.setCategory(product.getCategoryName());
            }

            // default values (offer, returnDay) because not in ProductBO
            rs.setOffer(0);
            rs.setReturnDay(0);

            // ===============================
            // Image URL from the FIRST Image
            // ===============================
            if (product.getImageFileIds() != null && !product.getImageFileIds().isEmpty()) {

                Long imageId = product.getImageFileIds().get(0);

                // URL generated from FileService or static path
                rs.setImageUrl("/api/files/" + imageId + "/view");
            }
        }

        return rs;
    }

    public static List<CartRs> mapToCartRsList(List<CartBO> list, FileService fileService) {

        if (list == null || list.isEmpty()) return List.of();

        return list.stream()
                .map(bo -> mapToCartRs(bo, fileService))
                .toList();
    }
}
