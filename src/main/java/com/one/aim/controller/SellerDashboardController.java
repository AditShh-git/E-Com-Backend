package com.one.aim.controller;

import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/overview")
    public BaseRs getSellerOverview() throws Exception {
        return sellerDashboardService.getSellerOverview();
    }

}
