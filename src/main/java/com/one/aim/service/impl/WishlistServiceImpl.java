package com.one.aim.service.impl;

import java.util.List;
import java.util.Optional;

import com.one.aim.bo.ProductBO;
import com.one.aim.repo.ProductRepo;
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

@Slf4j
@Service
public class WishlistServiceImpl implements WishlistService {

	@Autowired
	CartRepo cartRepo;

	@Autowired
	UserRepo userRepo;

    @Autowired
    ProductRepo productRepo;

//	@Autowired
//	WishlistRepo wishlistRepo;

    @Override
    public BaseRs addToWishlist(String productId) throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        UserBO user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductBO product = productRepo.findById(Long.parseLong(productId))
                .orElseThrow(() -> new RuntimeException("Invalid product ID"));

        // Avoid duplicates
        if (user.getWishlistProducts().contains(product)) {
            return ResponseUtils.failure("Already in wishlist");
        }

        user.getWishlistProducts().add(product);
        userRepo.save(user);

        return ResponseUtils.success(new WishlistDataRs("Added to wishlist"));
    }


    @Override
    public BaseRs getUserWishlist() throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        UserBO user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ProductBO> products = user.getWishlistProducts();

        List<WishlistRs> wishlistRsList = WishlistMapper.mapToWishlistRsList(products);

        return ResponseUtils.success(new WishlistDataRsList(
                MessageCodes.MC_RETRIEVED_SUCCESSFUL, wishlistRsList));
    }


    @Override
    public BaseRs deleteUserWishlist(String productId) throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        UserBO user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductBO product = productRepo.findById(Long.parseLong(productId))
                .orElseThrow(() -> new RuntimeException("Invalid product ID"));

        boolean removed = user.getWishlistProducts().remove(product);

        if (!removed) {
            return ResponseUtils.failure("Item not found in wishlist");
        }

        userRepo.save(user);

        return ResponseUtils.success(new WishlistDataRs(MessageCodes.MC_DELETED_SUCCESSFUL));
    }

}
