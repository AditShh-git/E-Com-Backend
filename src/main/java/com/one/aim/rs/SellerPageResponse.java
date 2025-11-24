package com.one.aim.rs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerPageResponse {
    private List<SellerRs> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}

