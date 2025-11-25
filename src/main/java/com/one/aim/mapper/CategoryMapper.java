package com.one.aim.mapper;

import com.one.aim.bo.CategoryBO;
import com.one.aim.rq.CategoryRq;
import com.one.aim.rs.CategoryRs;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    // Convert Entity → Response DTO
    public CategoryRs toRs(CategoryBO bo) {
        if (bo == null) return null;

        CategoryRs rs = new CategoryRs();
        rs.setId(bo.getId());
        rs.setName(bo.getName());
        rs.setActive(bo.isActive());
        return rs;
    }

    // Convert Request DTO → New Entity
    public CategoryBO toEntity(CategoryRq rq) {
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
}
