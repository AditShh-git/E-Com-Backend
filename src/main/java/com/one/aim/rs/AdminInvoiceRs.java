package com.one.aim.rs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminInvoiceRs {
    private Long invoiceId;
    private String invoiceNumber;
    private String orderId;
    private String invoiceDate;
    private Long totalAmount;

    private Long userId;
    private List<Long> sellerIds;
}

