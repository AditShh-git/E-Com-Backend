package com.one.aim.controller;

import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.repo.SellerRepo;
import com.one.aim.rs.UserRs;
import com.one.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.service.InvoiceService;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final SellerRepo  sellerRepo;

    // ================== DOWNLOAD INVOICE =====================
    @GetMapping("/download/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String orderId) throws Exception {

        UserRs logged = AuthUtils.findLoggedInUser();
        String role = logged.getRoll();
        Long loggedUserDbId = logged.getDocId();   // USER → DB id (Long)
        // SELLER → also DB id, but we will fetch sellerId using this

        // 1. Load Invoice
        InvoiceBO invoice = invoiceService.getInvoiceByOrderId(orderId);
        if (invoice == null) throw new RuntimeException("Invoice not found.");

        OrderBO order = invoice.getOrder();
        byte[] pdfBytes;

        switch (role.toUpperCase()) {

            // ==============================================
            // USER → Only download own invoice (Using DB ID)
            // ==============================================
            case "USER":
                if (!invoice.getUser().getId().equals(loggedUserDbId)) {
                    throw new RuntimeException("Not your invoice.");
                }

                pdfBytes = invoiceService.downloadInvoicePdf(orderId);
                break;

            // ==============================================
            // SELLER → Need sellerId (String), not DB ID
            // ==============================================
            case "SELLER":

                // Fetch sellerId (String) from DB using loggedUserDbId
                String loggedSellerStringId =
                        sellerRepo.findById(loggedUserDbId)
                                .orElseThrow(() -> new RuntimeException("Seller not found"))
                                .getSellerId();

                // Check order belongs to this seller
                boolean sellerMatch = order.getCartItems().stream()
                        .anyMatch(ci ->
                                ci != null &&
                                        ci.getProduct() != null &&
                                        ci.getProduct().getSeller() != null &&
                                        loggedSellerStringId.equals(ci.getProduct().getSeller().getSellerId())
                        );

                if (!sellerMatch) {
                    throw new RuntimeException("Not your order.");
                }

                pdfBytes = invoiceService.downloadSellerInvoicePdf(orderId, loggedSellerStringId);
                break;

            // ==============================================
            // ADMIN → Can download any invoice
            // ==============================================
            case "ADMIN":
                pdfBytes = invoiceService.downloadInvoicePdf(orderId);
                break;

            default:
                throw new RuntimeException("Invalid role.");
        }

        // ==============================================
        // RETURN PDF RESPONSE
        // ==============================================
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("invoice-" + orderId + ".pdf")
                        .build()
        );

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

}