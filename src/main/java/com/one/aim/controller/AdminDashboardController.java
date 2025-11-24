package com.one.aim.controller;

import com.one.aim.service.AdminDashboardService;
import com.one.vm.core.BaseRs;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public BaseRs getAdminDashboard() throws Exception {
        return adminDashboardService.getAdminOverview();
    }
}
