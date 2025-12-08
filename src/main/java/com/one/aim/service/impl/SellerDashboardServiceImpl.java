package com.one.aim.service.impl;

import java.util.*;

import com.one.aim.bo.SellerBO;
import com.one.aim.repo.ProductRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.rs.SellerOverviewRs;
import com.one.vm.analytics.TopProductVm;
import com.one.utils.AuthUtils;
import org.springframework.stereotype.Service;

import com.one.aim.repo.OrderRepo;
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
    private final SellerRepo sellerRepo;
    private final ProductRepo  productRepo;

    @Override
    public BaseRs getSellerOverview() throws Exception {

        SellerBO seller = sellerRepo.findByEmail(
                AuthUtils.findLoggedInUser().getEmail()
        ).orElseThrow(() -> new RuntimeException("Seller not found"));

        Long sellerId = seller.getId(); // numeric primary key from DB



        // ----------------------------------
        // TOTAL REVENUE (sum of order_items.total_price)
        // ----------------------------------
        Long totalRevenue = orderRepo.getTotalRevenueBySeller(sellerId);
        if (totalRevenue == null) totalRevenue = 0L;

        // ----------------------------------
        // TOTAL ORDERS (distinct orders for this seller)
        // ----------------------------------
        Long totalOrders = orderRepo.getSellerOrderCount(sellerId);
        if (totalOrders == null) totalOrders = 0L;

        Long totalProducts = productRepo.countProductsBySeller(sellerId);
        if (totalProducts == null) totalProducts = 0L;


        // ----------------------------------
        // RECENT ORDERS
        // ----------------------------------
        List<Object[]> rows = orderRepo.findRecentOrders(sellerId);

        List<RecentOrderVm> recentOrders = rows.stream().map(r ->
                new RecentOrderVm(
                        r[0].toString(),    // orderId
                        r[1].toString(),    // customerName
                        r[2].toString(),    // orderDate
                        r[3].toString(),    // orderStatus
                        ((Number) r[4]).doubleValue() // totalAmount
                )
        ).toList();

        // ----------------------------------
        // TOP SELLING PRODUCTS (From order_items)
        // ----------------------------------
        List<Object[]> topRows = orderRepo.getTopProducts(sellerId);

        List<TopProductVm> topProducts = topRows.stream()
                .map(r -> new TopProductVm(
                        r[0] != null ? r[0].toString() : "Unknown",
                        r[1] != null ? ((Number) r[1]).intValue() : 0
                ))
                .toList();


        // ----------------------------------
        // BUILD RESPONSE
        // ----------------------------------
        SellerOverviewRs.Stats stats = new SellerOverviewRs.Stats(
                totalRevenue.doubleValue(),
                totalOrders,
                Math.toIntExact(totalProducts),
                4.8 // static rating
        );

        SellerOverviewRs overview = new SellerOverviewRs(
                stats,
                recentOrders,
                topProducts
        );

        return buildSuccess("Seller dashboard fetched successfully", overview);
    }

    private BaseRs buildSuccess(String msg, Object payload) {
        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs(msg, payload));
        return base;
    }
}
