package com.one.aim.rs;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesPerformanceRs {

    private Long totalSalesLast30Days;
    private Integer percentChange;

    private List<WeeklyRevenueRs> weeklyRevenue;
}

