package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.one.aim.bo.CartBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.VendorBO;
import com.one.aim.rs.OrderRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderMapper {

	public static OrderRs mapToOrderRs(OrderBO bo) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToOrderRs(OrderBO) ->");
		}

		try {
			OrderRs rs = null;

			if (null == bo) {
				log.warn("UserBO is NULL");
				return rs;
			}
			rs = new OrderRs();

			rs.setDocId(String.valueOf(bo.getId()));
			long totalAmount = 0;
			if (Utils.isNotEmpty(bo.getCartItems())) {
				rs.setOrderedItems(CartMapper.mapToCartRsList(bo.getCartItems()));
				for (CartBO cartBO : bo.getCartItems()) {
					totalAmount = totalAmount + cartBO.getPrice();
				}
			}
			rs.setTotalAmount(totalAmount);
			rs.setOrderTime(bo.getOrderTime());
			rs.setPaymentMethod(bo.getPaymentMethod());
			rs.setUser(UserMapper.mapToUserRs(bo.getUser()));
			rs.setOrderStatus(bo.getOrderStatus());
			return rs;
		} catch (Exception e) {
			log.error("Exception in mapToOrderRs(OrderBO) - " + e);
			return null;
		}
	}

	public static List<OrderRs> mapToOrderRsList(List<OrderBO> bos) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToOrderRsList(OrderBO) ->");
		}

		try {
			if (Utils.isEmpty(bos)) {
				log.warn("OrderBO is NULL");
				return Collections.emptyList();
			}

			List<OrderRs> rsList = new ArrayList<>();
			for (OrderBO bo : bos) {
				OrderRs rs = mapToOrderRs(bo);
				if (null != rs) {
					rsList.add(rs);
				}
			}
			return rsList;
		} catch (Exception e) {
			log.error("Exception in mapToOrderRsList(OrderBO) - " + e);
			return Collections.emptyList();
		}
	}

	public static List<OrderRs> mapToOrderRsAdminList(List<OrderBO> bos, List<List<SellerBO>> sellerBOs,
			List<List<VendorBO>> vendorBOs) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToOrderRsList(OrderBO) ->");
		}

		try {
			if (Utils.isEmpty(bos)) {
				log.warn("OrderBO is NULL");
				return Collections.emptyList();
			}

			List<OrderRs> rsList = new ArrayList<>();
			int index = 0;
			for (OrderBO bo : bos) {
				OrderRs rs = mapToOrderAdminRs(bo, sellerBOs.get(index), vendorBOs.get(index));
				index++;
				if (null != rs) {
					rsList.add(rs);
				}
			}
			return rsList;
		} catch (Exception e) {
			log.error("Exception in mapToOrderRsList(OrderBO) - " + e);
			return Collections.emptyList();
		}
	}

	public static OrderRs mapToOrderAdminRs(OrderBO bo, List<SellerBO> sellerBOs, List<VendorBO> vendorBOs) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToOrderRs(OrderBO) ->");
		}

		try {
			OrderRs rs = null;

			if (null == bo) {
				log.warn("UserBO is NULL");
				return rs;
			}
			rs = new OrderRs();

			rs.setDocId(String.valueOf(bo.getId()));
			long totalAmount = 0;
			if (Utils.isNotEmpty(bo.getCartItems())) {
				rs.setOrderedItems(CartMapper.mapToCartRsList(bo.getCartItems()));
				for (CartBO cartBO : bo.getCartItems()) {
					totalAmount = totalAmount + cartBO.getPrice();
				}
			}
			rs.setTotalAmount(totalAmount);
			rs.setOrderTime(bo.getOrderTime());
			rs.setPaymentMethod(bo.getPaymentMethod());
			rs.setUser(UserMapper.mapToUserRs(bo.getUser()));
			return rs;
		} catch (Exception e) {
			log.error("Exception in mapToOrderRs(OrderBO) - " + e);
			return null;
		}
	}

}
