package com.one.aim.rs;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceRs {
    private Long invoiceId;
    private String invoiceNumber;
    private String orderId;
    private String invoiceDate;
    private Long totalAmount;
}
