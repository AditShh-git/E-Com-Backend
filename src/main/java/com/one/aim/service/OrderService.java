package com.one.aim.service;

import com.one.aim.rq.OrderRq;
import com.one.vm.core.BaseRs;

public interface OrderService {

	public BaseRs placeOrder(OrderRq rq) throws Exception;

	public BaseRs retrieveOrder(Long id) throws Exception;

	public BaseRs retrieveOrders() throws Exception;

	public void updateDeliveryStatus(String orderId, String status);

	public BaseRs retrieveOrdersUser() throws Exception;

	public BaseRs retrieveAllOrders() throws Exception;

    BaseRs cancelOrder(String orderId) throws Exception;

}
