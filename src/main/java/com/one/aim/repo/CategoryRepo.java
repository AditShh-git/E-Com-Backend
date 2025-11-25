package com.one.aim.repo;

import com.one.aim.bo.CategoryBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends JpaRepository<CategoryBO, Long> {

    boolean existsByNameIgnoreCase(String name);
}
