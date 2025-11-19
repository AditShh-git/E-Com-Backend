package com.one.vm.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderVm{
	private String orderId;
    private String customerName;
    private String date;
    private String status;
    private double total;
}
