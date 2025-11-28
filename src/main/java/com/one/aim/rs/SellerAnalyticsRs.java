package com.one.aim.rs;

import com.one.vm.analytics.DailyOrderCountVm;
import com.one.vm.analytics.OrderStatusVm;
import com.one.vm.analytics.SalesTrendVm;
import com.one.vm.analytics.TopProductChartVm;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerAnalyticsRs {

    private List<DailyOrderCountVm> recentOrdersActivity; // Bar chart
    private List<OrderStatusVm> orderStatus;              // Pie chart
    private List<TopProductChartVm> topProducts;          // Bar chart
    private List<SalesTrendVm> salesTrend;                // Line chart
}

