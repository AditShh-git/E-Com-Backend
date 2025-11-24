package com.one.aim.repo;

import com.one.aim.bo.WishlistBO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepo extends JpaRepository<WishlistBO, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    List<WishlistBO> findByUserId(Long userId);

    int countByUserId(Long userId);

    int deleteByUserIdAndProductId(Long userId, Long productId);
}

