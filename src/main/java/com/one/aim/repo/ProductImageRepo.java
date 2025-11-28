package com.one.aim.repo;

import com.one.aim.bo.ProductBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductImageRepo extends JpaRepository<ProductBO, Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM product_images WHERE product_id = :productId", nativeQuery = true)
    void deleteByProductId(@Param("productId") Long productId);
}

