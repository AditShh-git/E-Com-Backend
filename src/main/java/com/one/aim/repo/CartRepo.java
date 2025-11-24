package com.one.aim.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.CartBO;

@Repository
public interface CartRepo extends JpaRepository<CartBO, Long> {

    // -----------------------------
    // USER CART (Add-to-Cart items)
    // -----------------------------
    List<CartBO> findAllByUserAddToCart_Id(Long userId);

    // -----------------------------
    // SELLER CARTS
    // (sellerId is stored as Long inside CartBO)
    // -----------------------------
    List<CartBO> findAllBySellerId(String sellerId);

    // -----------------------------
    // SEARCH carts by product name
    // (pname = product snapshot name)
    // -----------------------------
    Page<CartBO> findByPnameContainingIgnoreCase(String pname, Pageable pageable);

    // -----------------------------
    // Filter by enabled flag
    // -----------------------------
    Page<CartBO> findAllByEnabledTrue(Pageable pageable);

    List<CartBO> findAllByEnabledTrue();

    Collection<Object> findByUserAddToCart_Id(Long userId);


    List<CartBO> findAllByUserAddToCartIdAndEnabledTrue(Long userId);

    Optional<CartBO> findByUserAddToCart_IdAndProduct_IdAndEnabledTrue(Long userId, Long productId);

    // -----------------------------
    // Category search (if needed)
    // NOTE: Only if CartBO has category removed,
    // this should be removed from service layer too.
    // -----------------------------
    //  Category does NOT exist anymore in CartBO
    // so DO NOT include findAllByCategory...
}
