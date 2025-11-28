package com.one.aim.controller;

import com.one.aim.rs.SellerAnalyticsRs;
import com.one.aim.service.SellerAnalyticsService;
import com.one.vm.analytics.SalesTrendVm;
import com.one.vm.analytics.TopProductChartVm;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller/analytics")
@RequiredArgsConstructor
@Slf4j
public class SellerAnalyticsController {

    private final SellerAnalyticsService sellerAnalyticsService;


    @GetMapping("")
    @PreAuthorize("hasAuthority('SELLER')")
    public BaseRs getAnalytics() {
        SellerAnalyticsRs payload = sellerAnalyticsService.getAnalytics();
        return ResponseUtils.success(payload);
    }


    @GetMapping("/charts/sales")
    @PreAuthorize("hasAuthority('SELLER')")
    public BaseRs getSalesTrend() {
        SellerAnalyticsRs analytics = sellerAnalyticsService.getAnalytics();
        List<SalesTrendVm> salesTrend = analytics == null ? List.of() : analytics.getSalesTrend();
        return ResponseUtils.success(salesTrend);
    }


    @GetMapping("/reports/products")
    @PreAuthorize("hasAuthority('SELLER')")
    public BaseRs getProductPerformance() {
        SellerAnalyticsRs analytics = sellerAnalyticsService.getAnalytics();
        List<TopProductChartVm> topProducts = analytics == null ? List.of() : analytics.getTopProducts();
        return ResponseUtils.success(topProducts);
    }
}

