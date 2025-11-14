package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.one.aim.bo.CartBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.VendorBO;
import com.one.aim.rs.CartMaxRs;
import com.one.aim.rs.CartRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CartMapper {

    // -------------------------
    // Single cart (min fields) with discount
    // -------------------------
    public static CartRs mapToCartMinRs(CartBO bo, int discountPercent, boolean discountEnabled) {
        if (bo == null) return null;

        CartRs rs = new CartRs();
        rs.setDocId(String.valueOf(bo.getId()));
        rs.setPName(bo.getPname());
        rs.setDescription(bo.getDescription());
        rs.setCategory(bo.getCategory());
        rs.setOffer(bo.getOffer());
        rs.setVarified(bo.isVarified());

        if (bo.getImage() != null) {
            rs.setImage(bo.getImage());
        }

        //  FIX 1: show correct stock
        rs.setTotalItem(bo.getTotalitem());

        //  FIX 2: show correct cart quantity (default 1)
        rs.setQuantity(bo.getQuantity() > 0 ? bo.getQuantity() : 1);

        long original = bo.getPrice();
        long discounted = calculateDiscountedPrice(original, discountPercent, discountEnabled);

        rs.setOriginalPrice(original);
        rs.setDiscountedPrice(discounted);
        rs.setPrice(discounted);

        return rs;
    }


    public static CartRs mapToCartMinRs(CartBO bo) {
        return mapToCartMinRs(bo, 0, false);
    }

    public static List<CartRs> mapToCartMinRsList(List<CartBO> bos, int discountPercent, boolean discountEnabled) {
        if (bos == null || bos.isEmpty()) return Collections.emptyList();
        List<CartRs> out = new ArrayList<>();
        for (CartBO b : bos) {
            CartRs r = mapToCartMinRs(b, discountPercent, discountEnabled);
            if (r != null) out.add(r);
        }
        return out;
    }

    public static List<CartRs> mapToCartMinRsList(List<CartBO> bos) {
        return mapToCartMinRsList(bos, 0, false);
    }

    // -------------------------
    // Full cart mapping
    // -------------------------
    public static CartRs mapToCartRs(CartBO bo, int discountPercent, boolean discountEnabled) {
        if (bo == null) return null;
        CartRs rs = new CartRs();
        rs.setDocId(String.valueOf(bo.getId()));
        rs.setPName(bo.getPname());
        rs.setDescription(bo.getDescription());
        rs.setCategory(bo.getCategory());
        rs.setTotalItem(bo.getTotalitem());
        rs.setSoldItem(bo.getSolditem());
        if (bo.getImage() != null) rs.setImage(bo.getImage());
        rs.setOffer(bo.getOffer());
        rs.setVarified(bo.isVarified());
        rs.setQuantity(bo.getQuantity());

        long original = bo.getPrice();
        long discounted = calculateDiscountedPrice(original, discountPercent, discountEnabled);

        rs.setOriginalPrice(original);
        rs.setDiscountedPrice(discounted);
        rs.setPrice(discounted);

        return rs;
    }

    public static CartRs mapToCartRs(CartBO bo) {
        return mapToCartRs(bo, 0, false);
    }

    public static List<CartRs> mapToCartRsList(List<CartBO> bos, int discountPercent, boolean discountEnabled) {
        if (bos == null || bos.isEmpty()) return Collections.emptyList();
        List<CartRs> out = new ArrayList<>();
        for (CartBO b : bos) {
            CartRs r = mapToCartRs(b, discountPercent, discountEnabled);
            if (r != null) out.add(r);
        }
        return out;
    }

    public static List<CartRs> mapToCartRsList(List<CartBO> bos) {
        return mapToCartRsList(bos, 0, false);
    }

    // -------------------------
    // Admin mapping (with sellers/vendors)
    // -------------------------
    public static CartMaxRs mapToCartAdminRs(CartBO bo, SellerBO sellerBO, VendorBO vendorBO,
                                             int discountPercent, boolean discountEnabled) {
        if (bo == null) return null;
        CartMaxRs rs = new CartMaxRs();
        if (sellerBO != null) rs.setSeller(SellerMapper.mapToSellerRs(sellerBO));
        if (vendorBO != null) rs.setVendor(VendorMapper.mapToVendorRs(vendorBO));

        rs.setDocId(String.valueOf(bo.getId()));
        rs.setPName(bo.getPname());
        rs.setDescription(bo.getDescription());
        rs.setCategory(bo.getCategory());
        rs.setTotalItem(bo.getTotalitem());
        rs.setSoldItem(bo.getSolditem());
        if (bo.getImage() != null) rs.setImage(bo.getImage());
        rs.setOffer(bo.getOffer());
        rs.setVarified(bo.isVarified());

        long original = bo.getPrice();
        long discounted = calculateDiscountedPrice(original, discountPercent, discountEnabled);

        rs.setOriginalPrice(original);
        rs.setDiscountPrice(discounted);

        rs.setPrice(discounted);


        return rs;
    }

    public static List<CartMaxRs> mapToCartAdminRsList(List<CartBO> bos, List<SellerBO> sellerBOs,
                                                       List<VendorBO> vendorBOs, int discountPercent, boolean discountEnabled) {
        if (bos == null || bos.isEmpty()) return Collections.emptyList();
        List<CartMaxRs> out = new ArrayList<>();
        for (int i = 0; i < bos.size(); i++) {
            SellerBO s = sellerBOs.size() > i ? sellerBOs.get(i) : null;
            VendorBO v = vendorBOs.size() > i ? vendorBOs.get(i) : null;
            out.add(mapToCartAdminRs(bos.get(i), s, v, discountPercent, discountEnabled));
        }
        return out;
    }

    // -------------------------
    // Helpers
    // -------------------------
    private static long calculateDiscountedPrice(long original, int discountPercent, boolean enabled) {
        if (!enabled || discountPercent <= 0) return original;
        long discounted = original - Math.round(original * (discountPercent / 100.0));
        return Math.max(0L, discounted);
    }
}

