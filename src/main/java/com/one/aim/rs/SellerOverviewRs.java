package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerOverviewRs {

    private Long totalSales;              // last 30 days revenue
    private Integer salesTrend;           // vs previous 30 days (%)

    private Double avgOrderValue;         // last 30 days
    private Integer aovTrend;             // vs previous 30 days (%)

    private Long customerAcquisition;     // unique customers last 30 days
    private Integer acqTrend;             // vs previous 30 days (%)

    private Double customerRetention;     // % customers with >=2 orders (last 30 days)
    private Integer retentionTrend;       // vs previous 30 days (%)
}


