package com.one.aim.service;

import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.vm.core.BaseRs;

import java.util.List;

public interface InvoiceService {

    // USER: Full invoice (HTML preview)
    String downloadInvoiceHtml(String orderId) throws Exception;

    // SELLER: Simplified invoice (HTML preview)
    String downloadSellerInvoiceHtml(String orderId, String sellerId) throws Exception;

    // USER / ADMIN: Download stored master PDF
    byte[] downloadInvoicePdf(String orderId) throws Exception;

    // SELLER: Generate PDF dynamically from HTML
    byte[] downloadSellerInvoicePdf(String orderId, String sellerId) throws Exception;

    // Create + store PDF invoice (User)
    InvoiceBO generateInvoice(String orderId) throws Exception;

    // Fetch invoice using public orderId
    InvoiceBO getInvoiceByOrderId(String orderId);

    // Lists
    List<InvoiceBO> getAllInvoicesForAdmin();
    List<InvoiceBO> getInvoicesForUser(Long userId);
    List<InvoiceBO> getInvoicesForSeller(String sellerId);
}
