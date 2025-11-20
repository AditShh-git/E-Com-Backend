package com.one.aim.service.impl;

import com.one.aim.repo.OrderItemBORepo;
import com.one.aim.repo.OrderRepo;
import com.one.aim.rs.*;
import com.one.aim.service.SellerAnalyticsService;
import com.one.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerAnalyticsServiceImpl implements SellerAnalyticsService {

    private final OrderItemBORepo orderItemRepo;
    private final OrderRepo orderRepo;

    @Override
    public SellerOverviewRs getOverview() {

        //  Logged-in seller ID (adjust per your AuthUtils)
        Long sellerId = AuthUtils.findLoggedInUser().getDocId();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currStart = now.minusDays(30);
        LocalDateTime prevStart = now.minusDays(60);
        LocalDateTime prevEnd = now.minusDays(30);

        // ------- current period -------
        Long currRevenue = orderItemRepo.getSellerTotalRevenue(sellerId, currStart, now);
        Long currOrders = orderRepo.countSellerOrders(sellerId, currStart, now);
        Long currUniqueCustomers = orderRepo.countSellerUniqueCustomers(sellerId, currStart, now);
        Long currReturningCustomers = orderRepo.countSellerReturningCustomers(sellerId, currStart, now);

        double currAov = (currOrders != null && currOrders > 0)
                ? currRevenue.doubleValue() / currOrders
                : 0.0;

        double currRetention = (currUniqueCustomers != null && currUniqueCustomers > 0)
                ? (currReturningCustomers * 100.0) / currUniqueCustomers
                : 0.0;

        // ------- previous period -------
        Long prevRevenue = orderItemRepo.getSellerTotalRevenue(sellerId, prevStart, prevEnd);
        Long prevOrders = orderRepo.countSellerOrders(sellerId, prevStart, prevEnd);
        Long prevUniqueCustomers = orderRepo.countSellerUniqueCustomers(sellerId, prevStart, prevEnd);
        Long prevReturningCustomers = orderRepo.countSellerReturningCustomers(sellerId, prevStart, prevEnd);

        double prevAov = (prevOrders != null && prevOrders > 0)
                ? prevRevenue.doubleValue() / prevOrders
                : 0.0;

        double prevRetention = (prevUniqueCustomers != null && prevUniqueCustomers > 0)
                ? (prevReturningCustomers * 100.0) / prevUniqueCustomers
                : 0.0;

        // ------- trends (for green/red indicator) -------
        int salesTrend = calculatePercentChange(currRevenue, prevRevenue);
        int aovTrend = calculatePercentChange(currAov, prevAov);
        int acqTrend = calculatePercentChange(
                safeLong(currUniqueCustomers),
                safeLong(prevUniqueCustomers)
        );
        int retentionTrend = calculatePercentChange(currRetention, prevRetention);

        return SellerOverviewRs.builder()
                .totalSales(currRevenue)
                .salesTrend(salesTrend)
                .avgOrderValue(currAov)
                .aovTrend(aovTrend)
                .customerAcquisition(currUniqueCustomers)
                .acqTrend(acqTrend)
                .customerRetention(currRetention)
                .retentionTrend(retentionTrend)
                .build();
    }

    @Override
    public SellerSalesTrendRs getSalesTrend() {

        Long sellerId = AuthUtils.findLoggedInUser().getDocId();
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        // DB: month -> revenue
        List<Object[]> rows = orderItemRepo.getSellerMonthlyRevenue(sellerId, year);

        // init map for 12 months
        Map<Integer, Long> monthlyMap = new HashMap<>();
        for (int m = 1; m <= 12; m++) {
            monthlyMap.put(m, 0L);
        }

        for (Object[] r : rows) {
            int month = ((Number) r[0]).intValue();
            Long revenue = ((Number) r[1]).longValue();
            monthlyMap.put(month, revenue);
        }

        List<MonthlyRevenueRs> monthlyList = new ArrayList<>();
        long totalSalesYear = 0L;

        for (int m = 1; m <= 12; m++) {
            Long rev = monthlyMap.get(m);
            totalSalesYear += rev;
            monthlyList.add(
                    MonthlyRevenueRs.builder()
                            .month(Month.of(m).name())  // "JAN", "FEB"...
                            .revenue(rev)
                            .build()
            );
        }

        int currentMonth = today.getMonthValue();
        Long currMonthRev = monthlyMap.get(currentMonth);
        Long prevMonthRev = monthlyMap.get(Math.max(currentMonth - 1, 1));

        int percentChange = calculatePercentChange(currMonthRev, prevMonthRev);

        return SellerSalesTrendRs.builder()
                .monthly(monthlyList)
                .totalSalesYear(totalSalesYear)
                .percentChange(percentChange)
                .build();
    }

    @Override
    public List<SellerProductRowRs> getProductPerformance() {

        Long sellerId = AuthUtils.findLoggedInUser().getDocId();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30);

        List<Object[]> rows = orderItemRepo.getSellerProductPerformance(sellerId, start, now);
        List<SellerProductRowRs> result = new ArrayList<>();

        for (Object[] r : rows) {
            String product = (String) r[0];
            String category = (String) r[1];
            Long qty = ((Number) r[2]).longValue();
            Long revenue = ((Number) r[3]).longValue();

            result.add(
                    SellerProductRowRs.builder()
                            .product(product)
                            .category(category)
                            .totalQuantity(qty)
                            .revenue(revenue)
                            .build()
            );
        }

        return result;
    }

    // ----------------- helpers -----------------

    private int calculatePercentChange(Long current, Long previous) {
        if (previous == null || previous == 0) return 0;
        double change = ((double) (current - previous) / previous) * 100;
        return (int) Math.round(change);
    }

    private int calculatePercentChange(Double current, Double previous) {
        if (previous == null || previous == 0.0) return 0;
        double change = ((current - previous) / previous) * 100;
        return (int) Math.round(change);
    }

    private Long safeLong(Long val) {
        return val == null ? 0L : val;
    }
}
