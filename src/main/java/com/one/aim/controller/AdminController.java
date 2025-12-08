package com.one.aim.controller;

import com.one.aim.bo.AdminBO;
import com.one.aim.bo.FileBO;
import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.mapper.InvoiceMapper;
import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.FileRepo;
import com.one.aim.repo.InvoiceRepo;
import com.one.aim.rq.LoginRq;
import com.one.aim.rs.AdminInvoiceRs;
import com.one.aim.rs.InvoiceRs;
import com.one.aim.service.AuthService;
import com.one.aim.service.InvoiceService;
import com.one.aim.service.ProductService;
import com.one.utils.AuthUtils;
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
    private final InvoiceService invoiceService;
    private final ProductService productService;

    // ============================================================
    // CREATE ADMIN (REGISTER)
    // ============================================================
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseRs> createAdmin(
            @ModelAttribute AdminRq rq,
            @RequestParam(value = "image", required = false) MultipartFile file
    ) throws Exception {

        if (file != null && !file.isEmpty()) {
            rq.setImage(file);
        }

        System.out.println("IMAGE RECEIVED? " + rq.getImage());
        return ResponseEntity.ok(adminService.createAdmin(rq));
    }



    // ============================================================
    // UPDATE ADMIN
    // ============================================================
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping(
            value = "/update",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BaseRs> updateAdmin(
            @ModelAttribute AdminRq rq,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws Exception {

        if (file != null && !file.isEmpty()) {
            rq.setImage(file);
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

    // =====================================================================
    //  ADMIN → Get ALL invoices
    // =====================================================================
    @GetMapping("/invoice/all")
    public List<AdminInvoiceRs> getAllInvoicesForAdmin() {

        String role = AuthUtils.findLoggedInUser().getRoll();

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Not allowed.");
        }

        return invoiceService.getAllInvoicesForAdmin()
                .stream()
                .map(InvoiceMapper::toAdminDto)
                .toList();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/product/list")
    public ResponseEntity<BaseRs> getAdminProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) throws Exception {
        return ResponseEntity.ok(productService.listAdminProducts(page, size, sortBy, direction));
    }



}
