package com.one.aim.mapper;

import com.one.aim.bo.InvoiceBO;
import com.one.aim.rs.AdminInvoiceRs;
import com.one.aim.rs.InvoiceRs;

import java.util.List;

public class InvoiceMapper {

    // Convert entity to DTO
    public static InvoiceRs toDto(InvoiceBO inv) {
        return new InvoiceRs(
                inv.getId(),
                inv.getInvoiceNumber(),
                inv.getOrder().getOrderId(),
                inv.getOrder().getOrderTime().toString(),
                inv.getOrder().getTotalAmount()
        );
    }

    public static AdminInvoiceRs toAdminDto(InvoiceBO inv) {
        List<Long> sellerIds = inv.getOrder().getCartItems()
                .stream()
                .map(ci -> ci.getProduct().getSeller().getId())
                .distinct()
                .toList();

        return new AdminInvoiceRs(
                inv.getId(),
                inv.getInvoiceNumber(),
                inv.getOrder().getOrderId(),
                inv.getOrder().getOrderTime().toString(),
                inv.getOrder().getTotalAmount(),
                inv.getUser().getId(),
                sellerIds
        );
    }

}
