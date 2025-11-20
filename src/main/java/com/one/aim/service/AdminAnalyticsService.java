package com.one.aim.service;

import com.one.aim.rs.*;
import com.one.vm.core.BaseRs;

import java.util.List;

public interface AdminAnalyticsService {

    // Main Dashboard (full pack)
    BaseRs getDashboard();

    // ---- Summary, Charts, Tables (Dashboard internal parts)
    SummaryCardsRs getSummaryCards();
    SalesPerformanceRs getSalesPerformance();
    UserActivityRs getUserActivity();
    List<SalesTableRowRs> getSalesTable();

    // ---- Overview Tab
    SummaryCardsRs getOverview();

    // ---- Sales Performance Tab
    SalesPerformanceRs getSalesChart();
    List<SalesTableRowRs> getSalesPerformanceReport();

    // ---- User Activity Tab
    UserActivityRs getUserActivityChart();
    List<UserActivityRowRs> getUserActivityReport();

    // ---- Marketing Effectiveness Tab
    List<MarketingEffectivenessRowRs> getMarketingEffectivenessReport();
}
