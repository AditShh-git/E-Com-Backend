package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryCardsRs {

    private Long totalRevenue;
    private Integer salesTrendPercent;
    private Long newUsers;
    private Long orderVolume;
    private String topSellingProduct;
}
