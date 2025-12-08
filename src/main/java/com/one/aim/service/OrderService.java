package com.one.aim.service;

import com.one.aim.rq.OrderRq;
import com.one.vm.core.BaseRs;

public interface OrderService {

	public BaseRs placeOrder(OrderRq rq) throws Exception;

    // Get single order by orderId (ORD-XXXXXX)
    BaseRs retrieveOrder(Long orderId) throws Exception;

    // Get paginated, sorted, filtered orders (Admin)
  public BaseRs retrieveOrders(
            int page,
            int size,
            String sortBy,
            String direction,
            String status
    ) throws Exception;

//	public BaseRs retrieveOrders() throws Exception;

	public void updateDeliveryStatus(String orderId, String status);

	public BaseRs retrieveOrdersUser() throws Exception;

	public BaseRs retrieveAllOrders() throws Exception;

    BaseRs cancelOrder(String orderId) throws Exception;

    BaseRs retrieveOrdersForSeller(int page, int size, String sortBy, String direction, String status) throws Exception;



}
