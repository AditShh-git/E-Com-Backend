package com.one.aim.service.impl;

import com.one.aim.bo.CategoryBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.mapper.CategoryMapper;
import com.one.aim.repo.CategoryRepo;
import com.one.aim.rq.CategoryRq;
import com.one.aim.rs.CategoryRs;
import com.one.aim.service.CategoryService;
import com.one.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final CategoryMapper mapper;

    @Override
    public CategoryRs createCategory(CategoryRq rq) {

        if (categoryRepo.existsByNameIgnoreCase(rq.getName())) {
            throw new BaseException(ErrorCodes.EC_CREATE_FAILED, "Category already exists");
        }

        CategoryBO bo = mapper.toEntity(rq);
        CategoryBO saved = categoryRepo.save(bo);

        return mapper.toRs(saved);
    }

    @Override
    public CategoryRs updateCategory(CategoryRq rq) {

        CategoryBO bo = categoryRepo.findById(rq.getId())
                .orElseThrow(() -> new BaseException(ErrorCodes.EC_RECORD_NOT_FOUND, "Category not found"));

        mapper.updateEntity(bo, rq);
        CategoryBO saved = categoryRepo.save(bo);

        return mapper.toRs(saved);
    }

    @Override
    public List<CategoryRs> getAllCategories() {
        return categoryRepo.findAll()
                .stream()
                .map(mapper::toRs)
                .toList();
    }

    @Override
    public List<CategoryRs> getActiveCategories() {
        return categoryRepo.findAll()
                .stream()
                .filter(CategoryBO::isActive)
                .map(mapper::toRs)
                .toList();
    }

    @Override
    public void deleteCategory(Long id) {

        if (!categoryRepo.existsById(id)) {
            throw new BaseException(ErrorCodes.EC_RECORD_NOT_FOUND, "Category not found");
        }

        categoryRepo.deleteById(id);
    }

    @Override
    public void deactivateCategory(Long id) {

        CategoryBO bo = categoryRepo.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCodes.EC_RECORD_NOT_FOUND, "Category not found"));

        bo.setActive(false);
        categoryRepo.save(bo);
    }
}