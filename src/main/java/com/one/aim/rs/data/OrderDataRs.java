package com.one.aim.rs.data;

import com.one.aim.rs.OrderRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderDataRs extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private OrderRs orderRs;

	public OrderDataRs(String message) {
		super(message);
	}

	public OrderDataRs(String message, OrderRs orderRs) {
		super(message);
		this.orderRs = orderRs;
	}

}
