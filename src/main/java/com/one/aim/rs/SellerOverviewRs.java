package com.one.aim.rs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.one.vm.analytics.TopProductVm;
import com.one.vm.common.RecentOrderVm;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class SellerOverviewRs {

    private Stats stats;
    private List<RecentOrderVm> recentOrders;
    private List<TopProductVm> topProducts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Stats {
        private Double totalRevenue;
        private Long totalOrders;
        private Integer totalProducts;
        private Double averageRating;
    }
}



