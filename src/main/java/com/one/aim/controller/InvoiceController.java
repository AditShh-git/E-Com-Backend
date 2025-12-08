package com.one.aim.controller;

import com.itextpdf.html2pdf.HtmlConverter;
import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.repo.SellerRepo;
import com.one.aim.rs.UserRs;
import com.one.utils.AuthUtils;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.service.InvoiceService;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final SellerRepo sellerRepo;

    // ================== DOWNLOAD INVOICE =====================
    @GetMapping("/download/{orderId}")
    public ResponseEntity<?> downloadInvoice(@PathVariable String orderId) {

        try {
            UserRs logged = AuthUtils.findLoggedInUser();
            String role = logged.getRoll();
            Long loggedUserDbId = logged.getDocId();

            //  Load Invoice Metadata
            InvoiceBO invoice = invoiceService.getInvoiceByOrderId(orderId);

            if (invoice == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseUtils.failure(
                                ErrorCodes.EC_INVOICE_NOT_FOUND,
                                "Invoice not found"
                        ));
            }

            byte[] pdfBytes;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            switch (role.toUpperCase()) {

                case "USER": {
                    // Only own invoice
                    if (!invoice.getUser().getId().equals(loggedUserDbId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ResponseUtils.failure(
                                        ErrorCodes.EC_UNAUTHORIZED,
                                        "You are not allowed to download this invoice"
                                ));
                    }

                    pdfBytes = invoiceService.downloadInvoicePdf(orderId);

                    headers.setContentDisposition(
                            ContentDisposition.attachment()
                                    .filename("invoice-" + orderId + ".pdf")
                                    .build()
                    );
                    break;
                }

                case "SELLER": {

                    SellerBO seller = sellerRepo.findById(loggedUserDbId)
                            .orElseThrow(() -> new RuntimeException("Seller not found"));

                    Long sellerDbId = seller.getId();

                    // Only invoice containing seller's products
                    pdfBytes = invoiceService.downloadSellerInvoicePdf(orderId, sellerDbId);

                    if (pdfBytes == null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ResponseUtils.failure(
                                        ErrorCodes.EC_UNAUTHORIZED,
                                        "This invoice does not contain your products"
                                ));
                    }

                    headers.setContentDisposition(
                            ContentDisposition.attachment()
                                    .filename("invoice-" + orderId + "-SEL-" + sellerDbId + ".pdf")
                                    .build()
                    );
                    break;
                }

                case "ADMIN": {

                    // Regenerate full invoice fresh
                    pdfBytes = invoiceService.downloadAdminInvoice(orderId);

                    headers.setContentDisposition(
                            ContentDisposition.attachment()
                                    .filename("invoice-" + orderId + "-ADMIN.pdf")
                                    .build()
                    );
                    break;
                }

                default:
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ResponseUtils.failure(
                                    ErrorCodes.EC_INVALID_USERROLE,
                                    "Invalid role for invoice access"
                            ));
            }

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseUtils.failure(
                            ErrorCodes.EC_INTERNAL_ERROR,
                            "Unable to process invoice request"
                    ));
        }
    }


}
