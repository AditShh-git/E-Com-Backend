package com.one.aim.rs;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerSalesTrendRs {

    private List<MonthlyRevenueRs> monthly;   // 12 entries: Jan–Dec
    private Integer percentChange;           // current month vs previous month (%)
    private Long totalSalesYear;             // sum of all 12 months
}


