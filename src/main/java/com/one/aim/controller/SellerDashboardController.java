package com.one.aim.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.rq.SellerDashboardRq;
import com.one.aim.service.SellerDashboardService;
import com.one.vm.core.BaseRs;

@RestController
@RequestMapping("/api/seller/dashboard")
public class SellerDashboardController {

    private final SellerDashboardService sellerDashboardService;

    public SellerDashboardController(SellerDashboardService sellerDashboardService) {
        this.sellerDashboardService = sellerDashboardService;
    }

    @PostMapping("/overview")
    public BaseRs getSellerDashboard(@RequestBody SellerDashboardRq rq) throws Exception {
        return sellerDashboardService.getSellerOverview(rq.getSellerId());
    }
}
