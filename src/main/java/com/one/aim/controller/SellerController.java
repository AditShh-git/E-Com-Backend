package com.one.aim.controller;

import com.one.aim.bo.InvoiceBO;
import com.one.aim.bo.OrderBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.mapper.InvoiceMapper;
import com.one.aim.repo.InvoiceRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.rq.LoginRq;
import com.one.aim.rq.SellerFilterRequest;
import com.one.aim.rs.InvoiceRs;
import com.one.aim.service.InvoiceService;
import com.one.utils.AuthUtils;
import com.one.vm.utils.ResponseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.rq.SellerRq;
import com.one.aim.service.SellerService;

import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
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
    private final SellerRepo  sellerRepo;

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
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSeller(@PathVariable String id) throws Exception {
        log.debug("Executing [DELETE /api/seller/{}]", id);
        return ResponseEntity.ok(sellerService.deleteSeller(id));
    }


    // =====================================================================
    //  SELLER → Get all invoices containing seller's products
    // =====================================================================
    @GetMapping("/invoice/all")
    public List<InvoiceRs> getSellerInvoices() {

        String role = AuthUtils.findLoggedInUser().getRoll();
        Long sellerDbId = AuthUtils.findLoggedInUser().getDocId();

        if (!"SELLER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Not allowed.");
        }

        SellerBO seller = sellerRepo.findById(sellerDbId)
                .orElseThrow(() -> new RuntimeException("Seller not found."));

        String sellerUniqueId = seller.getSellerId();

        return invoiceService.getInvoicesForSeller(sellerUniqueId)
                .stream()
                .map(InvoiceMapper::toDto)
                .toList();
    }

    @PutMapping(value = "/profile/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateSellerProfile(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNo,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) MultipartFile image,
            Principal principal
    ) throws Exception {

        log.debug("Executing [PUT /seller/profile/update]");

        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized: No seller logged in");
        }

        String sellerEmail = principal.getName();

        SellerRq rq = new SellerRq();
        rq.setFullName(fullName);
        rq.setPhoneNo(phoneNo);
        rq.setEmail(email);
        rq.setPassword(password);
        rq.setImage(image);

        return ResponseEntity.ok(sellerService.updateSellerProfile(sellerEmail, rq));
    }

}
