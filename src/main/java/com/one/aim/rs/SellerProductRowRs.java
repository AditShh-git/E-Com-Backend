package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProductRowRs {

    private String product;
    private String category;
    private Long totalQuantity;
    private Long revenue;
}

