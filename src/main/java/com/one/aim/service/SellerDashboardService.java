package com.one.aim.service;

import com.one.vm.core.BaseRs;

public interface SellerDashboardService {
    BaseRs getSellerOverview(String sellerId) throws Exception;
}
