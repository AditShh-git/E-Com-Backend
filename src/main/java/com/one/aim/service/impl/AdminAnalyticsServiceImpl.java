package com.one.aim.service.impl;

import com.one.aim.bo.SellerBO;
import com.one.aim.bo.UserActivityBO;
import com.one.aim.mapper.AdminAnalyticsMapper;
import com.one.aim.repo.*;
import com.one.aim.rs.*;
import com.one.aim.service.AdminAnalyticsService;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final OrderItemBORepo orderItemRepo;
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private final UserActivityRepo userActivityRepo;
    private final MarketingConversionRepo marketingConversionRepo;
    private final AdminAnalyticsMapper mapper;

    // ============================== DASHBOARD ==============================
    @Override
    public BaseRs getDashboard() {
        SummaryCardsRs summary = getSummaryCards();
        SalesPerformanceRs salesPerformance = getSalesPerformance();
        UserActivityRs userActivity = getUserActivity();
        List<SalesTableRowRs> salesTable = getSalesTable();

        return ResponseUtils.success(
                mapper.toAdminAnalyticsResponse(summary, salesPerformance, userActivity, salesTable)
        );
    }

    // ============================== SUMMARY ==============================
    @Override
    public SummaryCardsRs getSummaryCards() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30);

        // previous window = 30–60 days ago
        LocalDateTime prevStart = now.minusDays(60);
        LocalDateTime prevEnd = now.minusDays(30);

        // ================================
        // 1. Revenue + Trend %
        // ================================
        Long currRevenue = orderItemRepo.getTotalRevenue(start, now);
        Long prevRevenue = orderItemRepo.getTotalRevenue(prevStart, prevEnd);

        currRevenue = currRevenue == null ? 0L : currRevenue;
        prevRevenue = prevRevenue == null ? 0L : prevRevenue;

        int salesTrend = calculatePercentChange(currRevenue, prevRevenue);

        // ================================
        // 2. New Users
        // ================================
        Long newUsers = userRepo.countNewUsers(start, now);
        newUsers = newUsers == null ? 0L : newUsers;

        // ================================
        // 3. Order Volume
        // ================================
        Long orderVolume = orderRepo.getOrderVolume(start, now);
        orderVolume = orderVolume == null ? 0L : orderVolume;

        // ================================
        // 4. Top Selling Product
        // ================================
        List<Object[]> top = orderItemRepo.getTopSellingProduct(start, now);

        String topProduct = "N/A";
        if (top != null && !top.isEmpty()) {
            Object[] row = top.get(0);

            if (row != null && row.length > 0 && row[0] != null) {
                topProduct = row[0].toString();
            }
        }

        // ================================
        // FINAL RESPONSE
        // ================================
        return mapper.toSummaryCards(
                currRevenue,
                salesTrend,
                newUsers,
                orderVolume,
                topProduct
        );
    }


    // ============================== SALES PERFORMANCE ==============================
    @Override
    public SalesPerformanceRs getSalesPerformance() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30);

        Long totalSales = orderItemRepo.getTotalRevenue(start, now);
        Long prev = orderItemRepo.getTotalRevenue(now.minusDays(60), now.minusDays(30));
        int percentChange = calculatePercentChange(totalSales, prev);

        List<WeeklyRevenueRs> weekly = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            LocalDateTime wStart = now.minusDays(30 - (i * 7));
            LocalDateTime wEnd = wStart.plusDays(7);

            weekly.add(
                    mapper.toWeeklyRevenue("Week " + (i + 1),
                            orderItemRepo.getRevenueBetween(wStart, wEnd))
            );
        }

        return mapper.toSalesPerformance(totalSales, percentChange, weekly);
    }

    // ============================== USER ACTIVITY CHART ==============================
    @Override
    public UserActivityRs getUserActivity() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30);

        Long totalActiveUsers = orderRepo.getActiveUsers(start, now);
        Long prev = orderRepo.getActiveUsers(now.minusDays(60), now.minusDays(30));

        int percentChange = calculatePercentChange(totalActiveUsers, prev);

        List<WeeklyUserActivityRs> weekly = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            LocalDateTime wStart = now.minusDays(30 - (i * 7));
            LocalDateTime wEnd = wStart.plusDays(7);

            weekly.add(
                    mapper.toWeeklyUserActivity("Week " + (i + 1),
                            orderRepo.getActiveUsers(wStart, wEnd))
            );
        }

        return mapper.toUserActivity(totalActiveUsers, percentChange, weekly);
    }

    // ============================== SALES TABLE ==============================
    @Override
    public List<SalesTableRowRs> getSalesTable() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30);

        List<Object[]> rows = orderItemRepo.getSalesTable(start, now);
        List<SalesTableRowRs> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long sellerId = ((Number) row[2]).longValue();
            SellerBO seller = sellerRepo.findById(sellerId).orElse(null);
            result.add(mapper.toSalesTableRow(row, seller));
        }

        return result;
    }

    @Override
    public SummaryCardsRs getOverview() {
        // Overview Tab = Summary Cards
        return getSummaryCards();
    }

    @Override
    public SalesPerformanceRs getSalesChart() {
        // Sales Chart Tab = Sales Performance Chart
        return getSalesPerformance();
    }

    @Override
    public List<SalesTableRowRs> getSalesPerformanceReport() {
        // Sales Performance Table
        return getSalesTable();
    }

    @Override
    public UserActivityRs getUserActivityChart() {
        // User Activity Chart
        return getUserActivity();
    }


    // ============================== USER ACTIVITY REPORT (TABLE) ==============================
    @Override
    public List<UserActivityRowRs> getUserActivityReport() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30);

        List<UserActivityBO> logs = userActivityRepo.findByCreatedAtBetween(start, now);
        List<UserActivityRowRs> rows = new ArrayList<>();

        for (UserActivityBO log : logs) {
            rows.add(mapper.toUserActivityRow(log)); // NEW FIXED METHOD
        }

        return rows;
    }

    // ============================== MARKETING REPORT ==============================
    @Override
    public List<MarketingEffectivenessRowRs> getMarketingEffectivenessReport() {

        List<Object[]> rows = marketingConversionRepo.getCampaignAggregates();
        Long totalOrders = orderRepo.count();

        List<MarketingEffectivenessRowRs> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long campaignId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            Long orders = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Long revenue = row[3] != null ? ((Number) row[3]).longValue() : 0L;

            double rate = totalOrders > 0 ? (orders * 100.0) / totalOrders : 0.0;
            String sample = findSampleCouponCodeForCampaign(campaignId);

            result.add(mapper.toMarketingEffectivenessRow(name, orders, revenue, rate, sample));
        }

        return result;
    }

    private String findSampleCouponCodeForCampaign(Long id) {
        return marketingConversionRepo.findAll().stream()
                .filter(c -> c.getCampaign() != null &&
                        c.getCampaign().getId().equals(id) &&
                        c.getCoupon() != null)
                .map(c -> c.getCoupon().getCode())
                .findFirst()
                .orElse(null);
    }

    // ============================== PERCENT CHANGE ==============================
    private int calculatePercentChange(Long current, Long previous) {
        if (previous == null || previous == 0) return 0;
        double change = ((double) (current - previous) / previous) * 100;
        return (int) Math.round(change);
    }
}
