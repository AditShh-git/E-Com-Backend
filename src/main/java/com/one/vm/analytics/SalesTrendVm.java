package com.one.vm.analytics;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesTrendVm {
    private String day;     // e.g. "Jul 12"
    private Double sales;   // e.g. 2500.50
}
