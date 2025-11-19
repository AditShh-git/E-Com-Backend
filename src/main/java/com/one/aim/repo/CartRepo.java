package com.one.aim.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.CartBO;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartRepo extends JpaRepository<CartBO, Long> {

    // -----------------------------
    // USER CART (ONLY ACTIVE ITEMS)
    // -----------------------------
    List<CartBO> findAllByUserAddToCart_IdAndEnabled(Long userId, boolean enabled);

    Optional<CartBO> findByUserAddToCart_IdAndProduct_IdAndEnabled(Long userId, Long productId, boolean enabled);

    // -----------------------------
    // USER CART (ALL ITEMS - if needed)
    // -----------------------------
    List<CartBO> findAllByUserAddToCart_Id(Long userId);

    // -----------------------------
    // SELLER CART LIST
    // -----------------------------
    List<CartBO> findAllBySellerId(String sellerId);

    // -----------------------------
    // SEARCH BY product name snapshot
    // -----------------------------
    Page<CartBO> findByPnameContainingIgnoreCase(String pname, Pageable pageable);

    // -----------------------------
    // ENABLED ITEMS LIST
    // -----------------------------
    List<CartBO> findAllByEnabled(boolean enabled);

    Page<CartBO> findAllByEnabled(boolean enabled, Pageable pageable);

//    @Modifying
//    @Transactional
//    @Query("DELETE FROM CartBO c WHERE c.userAddToCart.id = :userId AND c.enabled = false")
//    void deleteOldDisabledCart(Long userId);


}
