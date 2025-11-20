package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTableRowRs {

    private String product;
    private String category;
    private String seller;
    private Long revenue;
}

