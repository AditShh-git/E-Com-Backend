package com.one.vm.analytics;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductChartVm {
    private String name;    // Product name
    private Long units;     // units sold
}
