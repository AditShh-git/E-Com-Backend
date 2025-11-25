package com.one.aim.repo;

import com.one.aim.bo.ProductBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<ProductBO, Long> {

    List<ProductBO> findAllBySeller_Id(Long sellerId);

    Optional<ProductBO> findBySlug(String slug);

    Page<ProductBO> findByCategoryNameIgnoreCase(String categoryName, Pageable pageable);

    Page<ProductBO> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsBySlug(String slug);
    
    long count();

    Page<ProductBO> findByCategoryId(Long categoryId, Pageable pageable);


}
