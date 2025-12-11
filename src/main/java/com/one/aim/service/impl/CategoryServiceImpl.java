package com.one.aim.service.impl;

import com.one.aim.bo.CategoryBO;
import com.one.aim.bo.FileBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.mapper.CategoryMapper;
import com.one.aim.repo.CategoryRepo;
import com.one.aim.repo.ProductRepo;
import com.one.aim.rq.CategoryRq;
import com.one.aim.rs.CategoryCardRs;
import com.one.aim.rs.CategoryRs;
import com.one.aim.service.CategoryService;
import com.one.aim.service.FileService;
import com.one.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final ProductRepo productRepo;
    private final CategoryMapper mapper;
    private final FileService fileService;

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
                .map(CategoryMapper::toRs)
                .toList();
    }

    @Override
    public List<CategoryRs> getActiveCategories() {
        return categoryRepo.findAll()
                .stream()
                .filter(CategoryBO::isActive)
                .map(CategoryMapper::toRs)
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

    @Override
    public Page<CategoryCardRs> getCategories(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        Page<CategoryBO> pageData = categoryRepo.findByActiveTrue(pageable);

        return pageData.map(cat -> {
            Long count = productRepo.countActiveProductsByCategory(cat.getId());
            return CategoryMapper.toCardRs(cat, count);
        });
    }

    @Override
    @Transactional
    public CategoryRs uploadCategoryImage(Long id, MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Image file is required");
        }

        // Validate size <= 2MB
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("Max image size is 2MB");
        }

        // Validate type JPG/PNG
        String type = file.getContentType();
        if (!List.of("image/jpeg", "image/png").contains(type)) {
            throw new RuntimeException("Only JPG or PNG allowed");
        }

        CategoryBO bo = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Delete previous image if exists
        if (bo.getImageFileId() != null) {
            fileService.deleteFileById(String.valueOf(bo.getImageFileId()));
        }

        FileBO uploaded = fileService.uploadAndReturnFile(file);
        bo.setImageFileId(uploaded.getId());

        categoryRepo.save(bo);

        return CategoryMapper.toRs(bo);
    }

    @Override
    @Transactional
    public CategoryRs deleteCategoryImage(Long id) throws Exception {

        CategoryBO bo = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (bo.getImageFileId() != null) {
            fileService.deleteFileById(String.valueOf(bo.getImageFileId()));
        }

        bo.setImageFileId(null);  // fallback default image
        categoryRepo.save(bo);

        return CategoryMapper.toRs(bo);
    }


}