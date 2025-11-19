package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.one.aim.bo.CartBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.bo.VendorBO;
import com.one.aim.rs.OrderRs;
import com.one.aim.service.FileService;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderMapper {

    public static OrderRs mapToOrderRs(OrderBO bo, FileService fileService) {

        if (bo == null) return null;

        OrderRs rs = new OrderRs();
        rs.setDocId(String.valueOf(bo.getId()));
        rs.setOrderId(bo.getOrderId());

        long totalAmount = 0;

        if (Utils.isNotEmpty(bo.getCartItems())) {
            rs.setOrderedItems(
                    CartMapper.mapToCartRsList(bo.getCartItems(), fileService)
            );

            for (CartBO cartBO : bo.getCartItems()) {
                totalAmount += cartBO.getPrice() * cartBO.getQuantity();
            }
        }

        rs.setTotalAmount(totalAmount);
        rs.setOrderTime(bo.getOrderTime());
        rs.setPaymentMethod(bo.getPaymentMethod());
        rs.setUser(UserMapper.mapToUserRs(bo.getUser()));
        rs.setOrderStatus(bo.getOrderStatus());

        return rs;
    }



    public static List<OrderRs> mapToOrderRsList(List<OrderBO> bos, FileService fileService) {

        if (log.isDebugEnabled()) {
            log.debug("Executing mapToOrderRsList(OrderBO) ->");
        }

        try {
            if (Utils.isEmpty(bos))
                return Collections.emptyList();

            List<OrderRs> rsList = new ArrayList<>();

            for (OrderBO bo : bos) {
                OrderRs rs = mapToOrderRs(bo, fileService);
                if (rs != null) rsList.add(rs);
            }

            return rsList;

        } catch (Exception e) {
            log.error("Exception in mapToOrderRsList(OrderBO) - " + e);
            return Collections.emptyList();
        }
    }
}
