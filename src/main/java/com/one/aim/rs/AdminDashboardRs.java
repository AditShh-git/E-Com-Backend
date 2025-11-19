package com.one.aim.rs;

import java.util.List;
import java.util.Map;

import com.one.vm.common.SellerListVm;
import com.one.vm.common.UserListVm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardRs {
	
	private long totalUsers;
    private long totalSellers;
    private long totalProducts;
    private long totalOrders;

    private long totalRevenue;
    
 // Charts
    private Map<String, Double> revenueTrends;   // month -> value
    private Map<String, Long> orderDistribution; // product -> count
    
 // Lists
    private List<UserListVm> users;
    private List<SellerListVm> sellers;
}
