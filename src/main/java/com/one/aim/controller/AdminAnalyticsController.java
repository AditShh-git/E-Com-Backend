package com.one.aim.controller;

import com.one.aim.service.AdminAnalyticsService;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    // ------------------------------------------------------------
    // ADMIN DASHBOARD
    // ------------------------------------------------------------
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BaseRs getDashboard() {
        try {
            return adminAnalyticsService.getDashboard();
        } catch (Exception ex) {
            log.error("Error fetching admin dashboard: {}", ex.getMessage(), ex);
            return ResponseUtils.failure("EC_ANALYTICS_ERROR", "Failed to load admin dashboard.");
        }
    }

    // ------------------------------------------------------------
    // ADMIN ANALYTICS OVERVIEW
    // ------------------------------------------------------------
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BaseRs getOverview() {
        return ResponseUtils.success(adminAnalyticsService.getOverview());
    }

    // ------------------------------------------------------------
    // SALES PERFORMANCE CHART
    // ------------------------------------------------------------
    @GetMapping("/charts/sales")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BaseRs getSalesChart() {
        return ResponseUtils.success(adminAnalyticsService.getSalesChart());
    }

    // ------------------------------------------------------------
    // USER ACTIVITY CHART
    // ------------------------------------------------------------
    @GetMapping("/charts/user-activity")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BaseRs getUserActivityChart() {
        return ResponseUtils.success(adminAnalyticsService.getUserActivityChart());
    }

    // ------------------------------------------------------------
    // SALES PERFORMANCE TABLE
    // ------------------------------------------------------------
    @GetMapping("/reports/sales-performance")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BaseRs getSalesPerformanceReport() {
        return ResponseUtils.success(adminAnalyticsService.getSalesPerformanceReport());
    }

    // USER ACTIVITY TABLE
    @GetMapping("/reports/user-activity")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BaseRs getUserActivityReport() {
        return ResponseUtils.success(adminAnalyticsService.getUserActivityReport());
    }

    //  MARKETING EFFECTIVENESS TABLE
    @GetMapping("/reports/marketing-effectiveness")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BaseRs getMarketingEffectivenessReport() {
        return ResponseUtils.success(adminAnalyticsService.getMarketingEffectivenessReport());
    }

}
