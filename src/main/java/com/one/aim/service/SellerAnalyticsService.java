package com.one.aim.service;

import com.one.aim.rs.SellerOverviewRs;
import com.one.aim.rs.SellerProductRowRs;
import com.one.aim.rs.SellerSalesTrendRs;

import java.util.List;

public interface SellerAnalyticsService {

    SellerOverviewRs getOverview();                     // top 4 cards
    SellerSalesTrendRs getSalesTrend();                 // 1-year monthly chart
    List<SellerProductRowRs> getProductPerformance();   // table

}
