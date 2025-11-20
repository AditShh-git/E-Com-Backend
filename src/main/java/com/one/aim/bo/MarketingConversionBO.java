package com.one.aim.bo;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketing_conversion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketingConversionBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many conversions → one campaign
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private MarketingCampaignBO campaign;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private CouponBO coupon;

    @Column(name = "revenue_generated", nullable = false)
    private Long revenueGenerated;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
