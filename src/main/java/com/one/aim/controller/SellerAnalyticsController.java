package com.one.aim.controller;

import com.one.aim.service.SellerAnalyticsService;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/analytics")
@RequiredArgsConstructor
@Slf4j
public class SellerAnalyticsController {

    private final SellerAnalyticsService sellerAnalyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('SELLER')")
    public BaseRs getOverview() {
        return ResponseUtils.success(sellerAnalyticsService.getOverview());
    }

    @GetMapping("/charts/sales")
    @PreAuthorize("hasAuthority('SELLER')")
    public BaseRs getSalesTrend() {
        return ResponseUtils.success(sellerAnalyticsService.getSalesTrend());
    }

    @GetMapping("/reports/products")
    @PreAuthorize("hasAuthority('SELLER')")
    public BaseRs getProductPerformance() {
        return ResponseUtils.success(sellerAnalyticsService.getProductPerformance());
    }
}


