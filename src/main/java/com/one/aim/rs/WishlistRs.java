package com.one.aim.rs;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistRs {

    private Long productId;
    private String productName;
    private Double price;
    private boolean inStock;
    private boolean lowStock;
    private String productImageUrl;   // First image
    private String categoryName;
}

