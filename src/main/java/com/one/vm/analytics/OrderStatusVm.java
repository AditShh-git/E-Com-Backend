package com.one.vm.analytics;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusVm {
    private String name;   // Delivered / Returned / Pending / Cancelled
    private Integer value; // Count
}
