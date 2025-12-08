package com.one.aim.service;

import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.vm.core.BaseRs;

import java.util.List;

public interface InvoiceService {

    // =====================================================
    // USER + ADMIN: Full invoice (HTML preview)
    // =====================================================
    String downloadInvoiceHtml(String orderId) throws Exception;

    // =====================================================
    // SELLER: Seller-specific invoice (HTML preview)
    // Only items belonging to sellerDbId
    // =====================================================
    String downloadSellerInvoiceHtml(String orderId, Long sellerDbId) throws Exception;

    // =====================================================
    // USER + ADMIN: Stored master PDF
    // =====================================================
    byte[] downloadInvoicePdf(String orderId) throws Exception;

    // =====================================================
    // SELLER: Live-generated PDF (from filtered HTML)
    // =====================================================
    byte[] downloadSellerInvoicePdf(String orderId, Long sellerDbId) throws Exception;

    // =====================================================
    // Create + Store master invoice PDF (for USER)
    // Called once when order succeeds
    // =====================================================
    InvoiceBO generateInvoice(String orderId) throws Exception;

    // =====================================================
    // Fetch invoice meta using public orderId
    // =====================================================
    InvoiceBO getInvoiceByOrderId(String orderId);

    // =====================================================
    // Listings (Panel/Pagination)
    // =====================================================
    List<InvoiceBO> getAllInvoicesForAdmin();

    List<InvoiceBO> getInvoicesForUser(Long userId);

    // SELLER using PK — most accurate
    List<InvoiceBO> getInvoicesForSeller(Long sellerDbId);

    byte[] downloadAdminInvoice(String orderId) throws Exception;
}
