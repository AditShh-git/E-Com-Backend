package com.one.vm.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductSalesVm {
    private String productName;
    private String day;
    private Double amount;
}