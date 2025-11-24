package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketingEffectivenessRowRs {

    private String campaignName;
    private Long orders;
    private Long revenueGenerated;
    private Double conversionRate;   // e.g. (campaignOrders / totalOrders) * 100
    private String sampleCouponCode; // any coupon code used in this campaign (if exists)
}
