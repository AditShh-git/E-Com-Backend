package com.one.aim.controller;

import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.repo.InvoiceRepo;
import com.one.aim.rq.LoginRq;
import com.one.aim.service.InvoiceService;
import com.one.utils.AuthUtils;
import com.one.vm.utils.ResponseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.rq.SellerRq;
import com.one.aim.service.SellerService;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@Slf4j
public class SellerController {

    private final SellerService sellerService;
    private final InvoiceService invoiceService;
    private final InvoiceRepo invoiceRepo;

    // ===========================================================
    // SELLER SIGN-UP (multipart/form-data)
    // ===========================================================
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveSeller(@ModelAttribute SellerRq rq) throws Exception {
        log.debug("Executing [POST /api/seller/signup]");
        return ResponseEntity.ok(sellerService.saveSeller(rq));
    }

    // ===========================================================
    // GET LOGGED-IN SELLER PROFILE
    // ===========================================================
    @GetMapping("/me")
    public ResponseEntity<?> retrieveSeller() throws Exception {
        log.debug("Executing [GET /api/seller/me]");
        return ResponseEntity.ok(sellerService.retrieveSeller());
    }

    // ===========================================================
    // GET ALL SELLERS (Admin only)
    // ===========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> retrieveSellers() throws Exception {
        log.debug("Executing [GET /api/seller/all]");
        return ResponseEntity.ok(sellerService.retrieveSellers());
    }

    // ===========================================================
    // LIST SELLER CARTS
    // ===========================================================
    @GetMapping("/carts")
    public ResponseEntity<?> retrieveSellerCarts() throws Exception {
        log.debug("Executing [GET /api/seller/carts]");
        return ResponseEntity.ok(sellerService.retrieveSellerCarts());
    }

    // ===========================================================
    // DELETE SELLER (Admin only)
    // ===========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSeller(@PathVariable String id) throws Exception {
        log.debug("Executing [DELETE /api/seller/{}]", id);
        return ResponseEntity.ok(sellerService.deleteSeller(id));
    }

//    // ===========================================================
//    // DOWNLOAD INVOICE FOR SELLER
//    // ===========================================================
//    @GetMapping("/download/{orderId}")
//    public ResponseEntity<byte[]> downloadInvoiceForSeller(@PathVariable Long orderId) throws Exception {
//
//        Long sellerId = AuthUtils.findLoggedInUser().getDocId();
//
//        InvoiceBO invoice = invoiceRepo.findByOrder_Id(orderId)
//                .orElseThrow(() -> new RuntimeException("Invoice not found"));
//
//        if (!invoice.getSeller().getId().equals(sellerId)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("ACCESS_DENIED: You cannot access this invoice.".getBytes());
//        }
//
//        byte[] pdfBytes = invoiceService.downloadInvoicePdf(orderId);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDisposition(ContentDisposition
//                .attachment()
//                .filename("invoice-" + invoice.getInvoiceNumber() + ".pdf")
//                .build());
//
//        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
//    }

//    // ===========================================================
//    // SELLER INVOICE LIST
//    // ===========================================================
//    @GetMapping("/all/invoices")
//    public ResponseEntity<?> getSellerInvoices() {
//
//        Long sellerId = AuthUtils.findLoggedInUser().getDocId();
//
//        List<InvoiceBO> invoices = invoiceRepo.findAllBySeller_Id(sellerId);
//
//        List<Map<String, Object>> response = invoices.stream().map(inv -> {
//            Map<String, Object> map = new HashMap<>();
//            map.put("invoiceId", inv.getId());
//            map.put("invoiceNo", inv.getInvoiceNumber());
//            map.put("orderId", inv.getOrder().getId());
//            map.put("amount", inv.getOrder().getTotalAmount());
//            map.put("date", inv.getCreatedAt());
//            map.put("downloadUrl", "/api/seller/download/" + inv.getOrder().getId());
//            return map;
//        }).collect(Collectors.toList());
//
//        return ResponseEntity.ok(response);
//    }
}
