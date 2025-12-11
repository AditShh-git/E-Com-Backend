package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ProductCardRs {

    private String docId;
    private String name;
    private String slug;

    private Double price;
    private String categoryName;
    private Long categoryId;

    private String image;
    private boolean inStock;

    private Double averageRating;
    private Long reviewCount;
}

