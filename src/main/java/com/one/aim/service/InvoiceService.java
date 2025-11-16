package com.one.aim.service;

import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.vm.core.BaseRs;

import java.util.List;

public interface InvoiceService {

    // USER invoice (already exists)
    String downloadInvoiceHtml(long orderId) throws Exception;

    // NEW: SELLER invoice HTML
    String downloadSellerInvoiceHtml(long orderId, long sellerId) throws Exception;

    // Stored PDF (user/admin)
    byte[] downloadInvoicePdf(Long orderId) throws Exception;

    // NEW: On-the-fly seller PDF
    byte[] downloadSellerInvoicePdf(Long orderId, Long sellerId) throws Exception;

    // Generate & store master invoice (user)
    InvoiceBO generateInvoice(Long orderId) throws Exception;

    // Role-based list APIs
    List<InvoiceBO> getAllInvoicesForAdmin();
    List<InvoiceBO> getInvoicesForSeller(Long sellerId);
    List<InvoiceBO> getInvoicesForUser(Long userId);

}
