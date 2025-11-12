package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.rs.OrderRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderDataRsList extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private List<OrderRs> orders;

	public OrderDataRsList(String message) {
		super(message);
	}

	public OrderDataRsList(String message, List<OrderRs> orders) {
		super(message);
		this.orders = orders;
	}

}
