package com.one.aim.mapper;

import com.one.aim.bo.SellerBO;
import com.one.aim.bo.UserActivityBO;
import com.one.aim.bo.UserBO;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.rs.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminAnalyticsMapper {

    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;

    // ------------------------------------------------------------
    // SUMMARY CARDS
    // ------------------------------------------------------------
    public SummaryCardsRs toSummaryCards(
            Long totalRevenue,
            Integer salesTrendPercent,
            Long newUsers,
            Long orderVolume,
            String topProduct
    ) {
        return SummaryCardsRs.builder()
                .totalRevenue(totalRevenue)
                .salesTrendPercent(salesTrendPercent)
                .newUsers(newUsers)
                .orderVolume(orderVolume)
                .topSellingProduct(topProduct)
                .build();
    }

    // ------------------------------------------------------------
    // SALES PERFORMANCE
    // ------------------------------------------------------------
    public SalesPerformanceRs toSalesPerformance(
            Long totalSalesLast30Days,
            Integer percentChange,
            List<WeeklyRevenueRs> weeklyRevenue
    ) {
        return SalesPerformanceRs.builder()
                .totalSalesLast30Days(totalSalesLast30Days)
                .percentChange(percentChange)
                .weeklyRevenue(weeklyRevenue)
                .build();
    }

    public WeeklyRevenueRs toWeeklyRevenue(String label, Long revenue) {
        return WeeklyRevenueRs.builder()
                .weekLabel(label)
                .revenue(revenue)
                .build();
    }

    // ------------------------------------------------------------
    // USER ACTIVITY TABLE (FINAL FIXED VERSION)
    // ------------------------------------------------------------
    public UserActivityRowRs toUserActivityRow(UserActivityBO activity) {

        Long actorId = activity.getUserId();

        // 1. Check user table
        UserBO user = userRepo.findById(actorId).orElse(null);
        if (user != null) {
            return UserActivityRowRs.builder()
                    .userName(user.getFullName())
                    .userId(user.getId())
                    .activityType(activity.getActivityType())
                    .description(activity.getDescription())
                    .time(activity.getCreatedAt())
                    .build();
        }

        // 2. Check seller table
        SellerBO seller = sellerRepo.findById(actorId).orElse(null);
        if (seller != null) {
            return UserActivityRowRs.builder()
                    .userName(seller.getFullName())
                    .userId(seller.getSellerId()) // BUSINESS ID (NOT DB ID)
                    .activityType(activity.getActivityType())
                    .description(activity.getDescription())
                    .time(activity.getCreatedAt())
                    .build();
        }

        // 3. Fallback
        return UserActivityRowRs.builder()
                .userName("Unknown")
                .userId(activity.getUserId())
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .time(activity.getCreatedAt())
                .build();
    }

    // ------------------------------------------------------------
// USER ACTIVITY CHART
// ------------------------------------------------------------
    public UserActivityRs toUserActivity(
            Long activeUsers,
            Integer percentChange,
            List<WeeklyUserActivityRs> weekly
    ) {
        return UserActivityRs.builder()
                .totalActiveUsers(activeUsers)
                .percentChange(percentChange)
                .weeklyActivity(weekly)
                .build();
    }

    // ------------------------------------------------------------
    // USER ACTIVITY CHART
    // ------------------------------------------------------------
    public WeeklyUserActivityRs toWeeklyUserActivity(String label, Long userCount) {
        return WeeklyUserActivityRs.builder()
                .weekLabel(label)
                .userCount(userCount)
                .build();
    }

    // ------------------------------------------------------------
    // SALES TABLE
    // ------------------------------------------------------------
    public SalesTableRowRs toSalesTableRow(Object[] row, SellerBO seller) {

        String product = (String) row[0];
        String category = (String) row[1];
        Long revenue = ((Number) row[3]).longValue();

        return SalesTableRowRs.builder()
                .product(product)
                .category(category)
                .seller(seller != null ? seller.getFullName() : "Unknown Seller")
                .revenue(revenue)
                .build();
    }

    // ------------------------------------------------------------
    // MARKETING TABLE
    // ------------------------------------------------------------
    public MarketingEffectivenessRowRs toMarketingEffectivenessRow(
            String campaignName,
            Long orders,
            Long revenueGenerated,
            Double conversionRate,
            String sampleCouponCode
    ) {
        return MarketingEffectivenessRowRs.builder()
                .campaignName(campaignName)
                .orders(orders)
                .revenueGenerated(revenueGenerated)
                .conversionRate(conversionRate)
                .sampleCouponCode(sampleCouponCode)
                .build();
    }

    // ------------------------------------------------------------
    // MAIN RESPONSE (DASHBOARD)
    // ------------------------------------------------------------
    public AdminAnalyticsRs toAdminAnalyticsResponse(
            SummaryCardsRs summary,
            SalesPerformanceRs performance,
            UserActivityRs userActivity,
            List<SalesTableRowRs> table
    ) {
        return AdminAnalyticsRs.builder()
                .summary(summary)
                .salesPerformance(performance)
                .userActivity(userActivity)
                .salesTable(table)
                .build();
    }

}
