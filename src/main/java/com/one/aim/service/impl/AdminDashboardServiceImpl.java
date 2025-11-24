package com.one.aim.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.one.aim.repo.OrderRepo;
import com.one.aim.repo.ProductRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.rs.AdminDashboardRs;
import com.one.aim.service.AdminDashboardService;
import com.one.vm.common.SellerListVm;
import com.one.vm.common.UserListVm;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseRs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

	
	private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    
	@Override
	public BaseRs getAdminOverview() throws Exception {

	    long totalUsers = userRepo.count();
	    long totalSellers = sellerRepo.count();
	    long totalProducts = productRepo.count();
	    long totalOrders = orderRepo.count();

	    Long totalRevenue = orderRepo.getTotalRevenue();
	    if (totalRevenue == null) totalRevenue = 0L;

	    LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
	    List<Object[]> trendData = orderRepo.getRevenueByMonth(sixMonthsAgo);

	    Map<String, Double> revenueTrends = new LinkedHashMap<>();
	    for (Object[] row : trendData) {
	        int month = (int) row[0];
	        double amount = ((Number) row[1]).doubleValue();
	        revenueTrends.put(getMonthName(month), amount * 1.15);
	    }

	    List<Object[]> distData = orderRepo.getOrderDistribution();
	    Map<String, Long> orderDistribution = new LinkedHashMap<>();
	    for (Object[] row : distData) {
	        orderDistribution.put(row[0].toString(), ((Number) row[1]).longValue());
	    }

	    List<UserListVm> users = userRepo.findAll().stream()
	            .map(u -> new UserListVm(
	                    u.getId(),
	                    u.getFullName(),
	                    u.getEmail(),
	                    u.getActive(),
	                    u.getCreatedAt()
	            )).toList();

	    List<SellerListVm> sellers = sellerRepo.findAll().stream()
	            .map(s -> new SellerListVm(
	                    s.getId(),
	                    s.getFullName(),
	                    s.getEmail(),
	                    s.isVerified(),
	                    s.getCreatedAt()
	            )).toList();

	    AdminDashboardRs dashboard = new AdminDashboardRs(
	            totalUsers,
	            totalSellers,
	            totalProducts,
	            totalOrders,
	            totalRevenue,
	            revenueTrends,
	            orderDistribution,
	            users,
	            sellers
	    );

	   // return new BaseRs("Admin dashboard fetched successfully", dashboard);
	    
	    BaseDataRs responseData = new BaseDataRs(
	            "Admin dashboard fetched successfully",
	            dashboard
	    );

	    BaseRs rs = new BaseRs();
	    rs.setStatus("SUCCESS");
	    rs.setData(responseData);
	    return rs;

	}

	private String getMonthName(int month) {
        return java.time.Month.of(month).name();
    }
	
}
