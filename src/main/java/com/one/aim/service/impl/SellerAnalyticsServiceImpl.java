package com.one.aim.service.impl;

import com.one.aim.repo.OrderItemBORepo;
import com.one.aim.rs.*;
import com.one.aim.service.SellerAnalyticsService;
import com.one.utils.AuthUtils;
import com.one.vm.analytics.DailyOrderCountVm;
import com.one.vm.analytics.OrderStatusVm;
import com.one.vm.analytics.SalesTrendVm;
import com.one.vm.analytics.TopProductChartVm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerAnalyticsServiceImpl implements SellerAnalyticsService {

    private final OrderItemBORepo orderItemRepo;

    @Override
    public SellerAnalyticsRs getAnalytics() {
        Long sellerId = AuthUtils.findLoggedInUser().getDocId();

        // We'll use a 30-day window for charts (you can change)
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);

        // --------------------
        // A. Sales trend (daily sales)
        // --------------------
        List<Object[]> dailySalesRows = orderItemRepo.getSellerDailySales(sellerId, start, end);
        List<SalesTrendVm> salesTrend = dailySalesRows.stream()
                .map(r -> {
                    // r[0] = java.sql.Date (or LocalDate depending on JPA), r[1] = Number
                    String day = r[0] == null ? "" : r[0].toString();
                    Double sales = r[1] == null ? 0.0 : ((Number) r[1]).doubleValue();
                    return new SalesTrendVm(day, sales);
                })
                .toList();

        // --------------------
        // B. Top selling products (last 30 days) — reusing existing repo method `findTopSelling`
        //    your findTopSelling(start,end,pageable) returns productId, productName, qty
        // --------------------
        List<Object[]> topRows = orderItemRepo.findTopSelling(start, end, PageRequest.of(0, 5));
        List<TopProductChartVm> topProducts = topRows.stream()
                .map(r -> {
                    // r[0] = productId (Long), r[1] = productName, r[2] = qty
                    String name = r.length > 1 && r[1] != null ? r[1].toString() : "Unknown";
                    Long units = r.length > 2 && r[2] != null ? ((Number) r[2]).longValue() : 0L;
                    return new TopProductChartVm(name, units);
                })
                .toList();

        // --------------------
        // C. Order status breakdown (pie)
        // --------------------
        List<Object[]> statusRows = orderItemRepo.getSellerOrderStatusSummary(sellerId, start, end);
        List<OrderStatusVm> orderStatus = statusRows.stream()
                .map(r -> {
                    String status = r[0] == null ? "UNKNOWN" : r[0].toString();
                    Integer count = r[1] == null ? 0 : ((Number) r[1]).intValue();
                    return new OrderStatusVm(status, count);
                })
                .toList();

        // --------------------
        // D. Recent orders activity (daily order count)
        // --------------------
        List<Object[]> dailyOrderRows = orderItemRepo.getSellerDailyOrderCount(sellerId, start, end);
        List<DailyOrderCountVm> recentActivity = dailyOrderRows.stream()
                .map(r -> {
                    String date = r[0] == null ? "" : r[0].toString();
                    Integer orders = r[1] == null ? 0 : ((Number) r[1]).intValue();
                    return new DailyOrderCountVm(date, orders);
                })
                .toList();

        // --------------------
        // Build and return
        // --------------------
        return new SellerAnalyticsRs(
                recentActivity,
                orderStatus,
                topProducts,
                salesTrend
        );
    }


}
