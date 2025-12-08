package com.one.aim.service.impl;

import java.util.List;
import java.util.Optional;

import com.one.aim.bo.ProductBO;
import com.one.aim.bo.WishlistBO;
import com.one.aim.repo.ProductRepo;
import com.one.aim.repo.WishlistRepo;
import com.one.aim.service.AdminSettingService;
import com.one.aim.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.one.aim.bo.CartBO;
import com.one.aim.bo.UserBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.mapper.WishlistMapper;
import com.one.aim.repo.CartRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.rs.WishlistRs;
import com.one.aim.rs.data.WishlistDataRs;
import com.one.aim.rs.data.WishlistDataRsList;
import com.one.aim.service.WishlistService;
import com.one.utils.AuthUtils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepo wishlistRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final CartService cartService;
    private final AdminSettingService adminSettingService;

    @Override
    public BaseRs add(Long productId) {

        // -----------------------------------------
        // CHECK: ADMIN DISABLED WISHLIST FEATURE
        // -----------------------------------------
        if (!adminSettingService.getBooleanValue("feature_wishlist_enabled", true)) {
            return ResponseUtils.failure("WISHLIST_DISABLED",
                    "Wishlist feature is currently disabled by admin.");
        }

        Long userId = AuthUtils.getLoggedUserId();

        UserBO user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductBO product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Already exists check
        boolean exists = wishlistRepo.existsByUserIdAndProductId(userId, productId);
        if (exists) {
            return ResponseUtils.failure("Already in wishlist");
        }

        WishlistBO w = new WishlistBO();
        w.setUser(user);
        w.setProduct(product);
        wishlistRepo.save(w);

        return ResponseUtils.success("Added to wishlist");
    }

    @Override
    @Transactional
    public BaseRs remove(Long productId) {

        Long userId = AuthUtils.getLoggedUserId();

        int deleted = wishlistRepo.deleteByUserIdAndProductId(userId, productId);

        if (deleted == 0) {
            return ResponseUtils.failure("Not found in wishlist");
        }

        return ResponseUtils.success("Removed");
    }

    @Override
    public BaseRs getAll() {

        Long userId = AuthUtils.getLoggedUserId();

        List<WishlistBO> items = wishlistRepo.findByUserId(userId);

        List<WishlistRs> rsList = items.stream()
                .map(i -> WishlistMapper.mapToWishlistRs(i.getProduct()))
                .toList();

        return ResponseUtils.success(rsList);
    }

    @Override
    @Transactional
    public BaseRs moveToCart(Long productId) throws Exception {

        Long userId = AuthUtils.getLoggedUserId();

        // Remove from wishlist
        wishlistRepo.deleteByUserIdAndProductId(userId, productId);

        // Add to cart
        cartService.addProductToCart(productId);

        return ResponseUtils.success("Moved to cart");
    }

    @Override
    public BaseRs count() {

        Long userId = AuthUtils.getLoggedUserId();
        int count = wishlistRepo.countByUserId(userId);

        return ResponseUtils.success(count);
    }
}
