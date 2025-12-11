package com.one.aim.mapper;

import com.one.aim.bo.CategoryBO;
import com.one.aim.rq.CategoryRq;
import com.one.aim.rs.CategoryCardRs;
import com.one.aim.rs.CategoryRs;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    // Convert Entity → Response DTO
    public static CategoryRs toRs(CategoryBO bo) {
        if (bo == null) return null;

        CategoryRs rs = new CategoryRs();
        rs.setId(bo.getId());
        rs.setName(bo.getName());
        rs.setActive(bo.isActive());
        return rs;
    }

    // Convert Request DTO → New Entity
    public static CategoryBO toEntity(CategoryRq rq) {
        if (rq == null) return null;

        return CategoryBO.builder()
                .name(rq.getName())
                .active(rq.isActive())
                .build();
    }

    // Update existing entity
    public void updateEntity(CategoryBO bo, CategoryRq rq) {
        if (bo == null || rq == null) return;

        bo.setName(rq.getName());
        bo.setActive(rq.isActive());
    }

    public static CategoryCardRs toCardRs(CategoryBO bo, Long productCount) {

        if (bo == null) return null;

        CategoryCardRs rs = new CategoryCardRs();
        rs.setId(bo.getId());
        rs.setName(bo.getName());

        // Always set an image (fallback placeholder if null)
        String img = (bo.getImageFileId() != null)
                ? "/api/files/public/" + bo.getImageFileId() + "/view"
                : "/assets/img/category-default.png"; // 🔹 Static placeholder
        rs.setImage(img);

        rs.setProductCount(productCount == null ? 0 : productCount);

        return rs;
    }

}
