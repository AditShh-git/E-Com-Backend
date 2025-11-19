package com.one.aim.rs;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.one.vm.common.RecentOrderVm;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SellerDashboardRs {
	
	 private Long totalSales;           // total sales amount
	 private Long totalOrders;          // total number of orders
	 private Double averageOrderValue;  // avg order value

	 private Double salesChangePercent;   // % increment/decrement
	 private Double orderChangePercent;   // % increment/decrement
	 private Double avgOrderChangePercent;

	 private List<RecentOrderVm> recentOrders;

	 // product -> { day -> amount }
	 private Map<String, Map<String, Double>> productSalesByDay;

	 private Double totalSalesToday;
	 private Double totalSalesTodayChangePercent;

}
