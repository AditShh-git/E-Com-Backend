package com.one.aim.controller;

import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.rs.UserRs;
import com.one.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.one.aim.service.InvoiceService;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    // --------------------------------------------
    // Download PDF (User / Seller / Admin)
    // --------------------------------------------
    @GetMapping("/download/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId) throws Exception {

        Long loggedUserId = AuthUtils.findLoggedInUser().getDocId();
        String role = AuthUtils.findLoggedInUser().getRoll();

        // 1. Get Invoice
        InvoiceBO invoice = invoiceService.getAllInvoicesForAdmin().stream()
                .filter(i -> i.getOrder().getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        OrderBO order = invoice.getOrder();

        // 2. ROLE VALIDATION
        switch (role.toUpperCase()) {

            case "USER":
                // USER can access only their own order
                if (!invoice.getUser().getId().equals(loggedUserId))
                    throw new RuntimeException("Not allowed: This invoice does not belong to you.");
                break;

            case "SELLER":
                // SELLER must match at least one cart item seller
                boolean sellerMatch =
                        order.getCartItems().stream()
                                .anyMatch(ci -> ci.getProduct().getSeller().getId().equals(loggedUserId));

                if (!sellerMatch)
                    throw new RuntimeException("Not allowed: You are not the seller for this order.");
                break;

            case "ADMIN":
                // ADMIN can always download
                break;

            default:
                throw new RuntimeException("Invalid role. Access denied.");
        }

        // 3. Load the PDF
        byte[] pdf = invoiceService.downloadInvoicePdf(orderId);

        if (pdf == null || pdf.length < 50)
            throw new RuntimeException("Invoice PDF is missing or corrupted.");

        // 4. Return file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("invoice-" + orderId + ".pdf")
                .build());

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // -------------------------
    // ADMIN: View all invoices
    // -------------------------
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllInvoicesAdmin() {
        return ResponseEntity.ok(invoiceService.getAllInvoicesForAdmin());
    }

    // -------------------------
    // SELLER: View own invoices
    // -------------------------
    @GetMapping("/seller/my")
    public ResponseEntity<?> getSellerInvoices() {

        Long sellerId = AuthUtils.findLoggedInUser().getDocId();
        String role = AuthUtils.findLoggedInUser().getRoll();

        if (!"SELLER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Only sellers can view invoices.");
        }

        return ResponseEntity.ok(invoiceService.getInvoicesForSeller(sellerId));
    }

    // -------------------------
    // USER: View their invoices
    // -------------------------
    @GetMapping("/user/my")
    public ResponseEntity<?> getUserInvoices() {

        Long userId = AuthUtils.findLoggedInUser().getDocId();
        String role = AuthUtils.findLoggedInUser().getRoll();

        if (!"USER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Only users can view invoices.");
        }

        return ResponseEntity.ok(invoiceService.getInvoicesForUser(userId));
    }
}
