package com.one.aim.controller;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.FileBO;
import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.FileRepo;
import com.one.aim.repo.InvoiceRepo;
import com.one.aim.rq.LoginRq;
import com.one.aim.service.AuthService;
import com.one.aim.service.InvoiceService;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.rq.AdminRq;
import com.one.aim.service.AdminService;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final InvoiceRepo invoiceRepo;
    private final FileRepo fileRepo;

    // ============================================================
    // CREATE ADMIN (REGISTER)
    // ============================================================
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseRs> createAdmin(
            @ModelAttribute AdminRq rq,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws Exception {

        log.debug("Executing [POST /api/admin/create]");

        if (file != null && !file.isEmpty()) {
            rq.setImage(file.getBytes());
        }

        log.info("Admin registration request for email: {}", rq.getEmail());

        return ResponseEntity.ok(adminService.createAdmin(rq));
    }

    // ============================================================
    // UPDATE ADMIN
    // ============================================================
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseRs> updateAdmin(
            @ModelAttribute AdminRq rq,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws Exception {

        log.debug("Executing [PUT /api/admin/update]");

        if (file != null && !file.isEmpty()) {
            rq.setImage(file.getBytes());
        }

        return ResponseEntity.ok(adminService.updateAdmin(rq));
    }

    // ============================================================
    // GET ADMIN PROFILE
    // ============================================================
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<BaseRs> getAdminProfile() throws Exception {
        log.debug("Executing [GET /api/admin/me]");
        return ResponseEntity.ok(adminService.retrieveAdmin());
    }

//    // ============================================================
//    // ADMIN LOGOUT
//    // ============================================================
//    @PreAuthorize("hasAuthority('ADMIN')")
//    @PostMapping("/logout")
//    public ResponseEntity<BaseRs> adminLogout() throws Exception {
//        log.debug("Executing [POST /api/admin/logout]");
//        return ResponseEntity.ok(adminService.logout());
//    }

//    // ============================================================
//    // DOWNLOAD INVOICE
//    // ============================================================
//    @PreAuthorize("hasAuthority('ADMIN')")
//    @GetMapping(
//            value = "/download/{orderId}",
//            produces = MediaType.APPLICATION_PDF_VALUE
//    )
//    public ResponseEntity<byte[]> downloadInvoiceForAdmin(@PathVariable Long orderId) throws Exception {
//
//        InvoiceBO invoice = invoiceRepo.findByOrder_Id(orderId)
//                .orElseThrow(() -> new RuntimeException("Invoice not found"));
//
//        FileBO file = fileRepo.findById(invoice.getInvoiceFileId())
//                .orElseThrow(() -> new RuntimeException("Invoice PDF file missing"));
//
//        if (file.getInputstream() == null) {
//            throw new RuntimeException("PDF content empty");
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDisposition(
//                ContentDisposition.attachment()
//                        .filename("invoice-" + invoice.getInvoiceNumber() + ".pdf")
//                        .build()
//        );
//
//        return new ResponseEntity<>(file.getInputstream(), headers, HttpStatus.OK);
//    }
//
//    // ============================================================
//    // VIEW ALL INVOICES LIST
//    // ============================================================
//    @PreAuthorize("hasAuthority('ADMIN')")
//    @GetMapping("/all/invoices")
//    public ResponseEntity<?> getAllInvoicesForAdmin() {
//
//        List<InvoiceBO> invoices = invoiceRepo.findAll();
//
//        List<Map<String, Object>> response = invoices.stream().map(inv -> {
//
//            OrderBO order = inv.getOrder();
//
//            // Collect list of seller names
//            String sellers = order.getCartItems().stream()
//                    .map(ci -> ci.getProduct().getSeller().getFullName())
//                    .distinct()
//                    .collect(Collectors.joining(", "));
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("invoiceId", inv.getId());
//            map.put("invoiceNo", inv.getInvoiceNumber());
//            map.put("orderId", order.getId());
//            map.put("amount", order.getTotalAmount());
//            map.put("user", inv.getUser().getFullName());
//            map.put("sellers", sellers);   // ✔ multi-seller safe
//            map.put("date", inv.getCreatedAt());
//            map.put("downloadUrl", "/api/admin/download/" + order.getId());
//
//            return map;
//
//        }).collect(Collectors.toList());
//
//        return ResponseEntity.ok(response);
//    }

}
