package com.one.aim.service;

import com.one.aim.rq.CategoryRq;
import com.one.aim.rs.CategoryRs;

import java.util.List;

public interface CategoryService {

    CategoryRs createCategory(CategoryRq rq);

    CategoryRs updateCategory(CategoryRq rq);

    List<CategoryRs> getAllCategories();           // admin view

    List<CategoryRs> getActiveCategories();        // seller dropdown

    void deleteCategory(Long id);                  // hard delete

    void deactivateCategory(Long id);              // soft delete
}
