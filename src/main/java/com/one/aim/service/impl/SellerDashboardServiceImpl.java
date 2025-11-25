package com.one.aim.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.one.aim.repo.SellerRepo;
import com.one.utils.AuthUtils;
import org.springframework.stereotype.Service;

import com.one.aim.repo.OrderRepo;
import com.one.aim.repo.ProductRepo;
import com.one.aim.rs.SellerDashboardRs;
import com.one.aim.service.SellerDashboardService;
import com.one.vm.common.RecentOrderVm;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseRs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerDashboardServiceImpl implements SellerDashboardService {

	private final OrderRepo orderRepo;
    private final SellerRepo  sellerRepo;


    @Override
    public BaseRs getSellerOverview() throws Exception {

        Long sellerId = AuthUtils.findLoggedInUser().getDocId();   // DB ID (Long)

        // ---------- TOTALS ----------
        Long totalSales = orderRepo.getTotalSalesBySeller(sellerId);
        Long totalOrders = orderRepo.countBySellerId(sellerId);

        if (totalSales == null) totalSales = 0L;

        Double avgOrderValue = totalOrders == 0 ? 0.0 : (double) totalSales / totalOrders;

        // ---------- PERCENTAGE CHANGE ----------
        Long lastMonthSales = orderRepo.getLastMonthSalesBySeller(sellerId);
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        Long lastMonthOrders = orderRepo.getLastMonthOrderCountBySeller(sellerId, lastMonth);

        Double salesChange = percentChange(lastMonthSales, totalSales);
        Double orderChange = percentChange(lastMonthOrders, totalOrders);

        Double lastMonthAov = lastMonthOrders == 0 ? 0.0 : (double) lastMonthSales / lastMonthOrders;
        Double aovChange = percentChange(lastMonthAov, avgOrderValue);

        // ---------- RECENT ORDERS ----------
        List<Object[]> orderRows = orderRepo.findRecentOrdersBySeller(sellerId);
        List<RecentOrderVm> recentOrders = orderRows.stream()
                .map(r -> new RecentOrderVm(
                        r[0].toString(),
                        r[1].toString(),
                        r[2].toString(),
                        r[3].toString(),
                        ((Number) r[4]).doubleValue()
                )).toList();

        // ---------- PRODUCT SALES ----------
        List<Object[]> chartData = orderRepo.getSalesByProductPerDay(sellerId);

        Map<String, Map<String, Double>> productSales = new LinkedHashMap<>();
        for (Object[] row : chartData) {
            String product = row[0].toString();
            String day = row[1].toString();
            Double amount = ((Number) row[2]).doubleValue();

            productSales.computeIfAbsent(product, k -> new LinkedHashMap<>()).put(day, amount);
        }

        // ---------- TODAY SALES ----------
        Double todaySales = orderRepo.getTodaySales(sellerId);
        if (todaySales == null) todaySales = 0.0;

        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusSeconds(1);
        Double yesterdaySales = orderRepo.getSalesBetween(sellerId, start, end);
        Double todayChange = percentChange(yesterdaySales, todaySales);

        // ---------- RESPONSE ----------
        SellerDashboardRs rs = new SellerDashboardRs(
                totalSales,
                totalOrders,
                avgOrderValue,
                salesChange,
                orderChange,
                aovChange,
                recentOrders,
                productSales,
                todaySales,
                todayChange
        );

        return buildSuccess("Seller dashboard fetched successfully", rs);

    }



    private Double percentChange(Number oldValue, Number newValue) {
	        double oldV = oldValue == null ? 0 : oldValue.doubleValue();
	        double newV = newValue == null ? 0 : newValue.doubleValue();

	        if (oldV == 0) return newV > 0 ? 100.0 : 0.0;

	        return ((newV - oldV) / oldV) * 100.0;
	    }

    private BaseRs buildSuccess(String msg, Object payload) {
        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs(msg, payload));
        return base;
    }


}
