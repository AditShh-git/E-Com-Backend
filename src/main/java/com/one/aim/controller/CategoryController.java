package com.one.aim.controller;

import com.one.aim.constants.MessageCodes;
import com.one.aim.rq.CategoryRq;
import com.one.aim.rs.CategoryRs;
import com.one.aim.service.CategoryService;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseRs;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ================================
    // CREATE CATEGORY (ADMIN ONLY)
    // ================================
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create")
    public BaseRs create(@RequestBody CategoryRq rq) {

        CategoryRs rs = categoryService.createCategory(rq);

        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs(MessageCodes.MC_CREATED_SUCCESSFUL, rs));
        return base;
    }

    // ================================
    // UPDATE CATEGORY (ADMIN ONLY)
    // ================================
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update")
    public BaseRs update(@RequestBody CategoryRq rq) {

        CategoryRs rs = categoryService.updateCategory(rq);

        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs(MessageCodes.MC_UPDATED_SUCCESSFULLY, rs));
        return base;
    }

    // ================================
    // GET ALL CATEGORIES (ADMIN ONLY)
    // ================================
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public BaseRs getAll() {

        List<CategoryRs> list = categoryService.getAllCategories();

        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, list));
        return base;
    }

    // =====================================
    // GET ACTIVE CATEGORIES (ADMIN + SELLER)
    // =====================================
    @PreAuthorize("hasAnyAuthority('ADMIN','SELLER')")
    @GetMapping("/active")
    public BaseRs getActive() {

        List<CategoryRs> list = categoryService.getActiveCategories();

        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, list));
        return base;
    }

    // ================================
    // DELETE CATEGORY (ADMIN ONLY)
    // ================================
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public BaseRs delete(@PathVariable Long id) {

        categoryService.deleteCategory(id);

        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs(MessageCodes.MC_DELETED_SUCCESSFUL, null));
        return base;
    }

    // =====================================
    // DEACTIVATE CATEGORY (ADMIN ONLY)
    // =====================================
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/deactivate/{id}")
    public BaseRs deactivate(@PathVariable Long id) {

        categoryService.deactivateCategory(id);

        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs(MessageCodes.MC_UPDATED_SUCCESSFULLY, null));
        return base;
    }
}
