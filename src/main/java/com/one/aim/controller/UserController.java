package com.one.aim.controller;

import com.one.aim.bo.FileBO;
import com.one.aim.bo.InvoiceBO;
import com.one.aim.repo.FileRepo;
import com.one.aim.repo.InvoiceRepo;
import com.one.aim.rq.UpdateRq;
import com.one.aim.service.AuthService;
import com.one.aim.service.InvoiceService;
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

import com.one.aim.rq.LoginRq;
import com.one.aim.rq.UserRq;
import com.one.aim.service.UserService;
import com.one.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final InvoiceService invoiceService;
    private final InvoiceRepo invoiceRepo;
    private final FileRepo fileRepo;

    // ===========================================================
    // USER SIGNUP
    // ===========================================================
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(
            @ModelAttribute UserRq rq,
            @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

        log.debug("Executing [POST /api/user/signup]");

        if (file != null && !file.isEmpty()) {
            rq.setImage(file);
        }

        log.info("Processing registration for email: {}", rq.getEmail());
        return ResponseEntity.ok(userService.saveUser(rq));
    }

    // ===========================================================
    // GET CURRENT LOGGED-IN USER
    // ===========================================================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() throws Exception {
        log.debug("Executing [GET /api/user/me]");
        return ResponseEntity.ok(userService.retrieveUser());
    }

    // ===========================================================
    // GET ALL USERS (ADMIN ONLY)
    // ===========================================================
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() throws Exception {
        log.debug("Executing [GET /api/user/all]");
        return ResponseEntity.ok(userService.retrieveAllUser());
    }

    // ===========================================================
    // DELETE USER (ADMIN ONLY)
    // ===========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) throws Exception {
        log.debug("Executing [DELETE /api/user/{}]", id);
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    // ===========================================================
    // UPDATE USER PROFILE
    // ===========================================================
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping(value = "/profile/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseRs> updateProfile(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNo,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String oldPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            @RequestParam(required = false) MultipartFile image
    ) throws Exception {

        log.debug("Executing [PUT /api/user/profile/update]");

        UpdateRq rq = new UpdateRq();
        rq.setFullName(fullName);
        rq.setPhoneNo(phoneNo);
        rq.setEmail(email);
        rq.setOldPassword(oldPassword);
        rq.setNewPassword(newPassword);
        rq.setConfirmPassword(confirmPassword);
        rq.setImage(image);

        String loggedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(userService.updateUserProfile(loggedEmail, rq));
    }

//    // ===========================================================
//    // DOWNLOAD INVOICE FOR USER
//    // ===========================================================
//    @GetMapping("/download/{orderId}")
//    public ResponseEntity<byte[]> downloadInvoiceForUser(@PathVariable Long orderId) throws Exception {
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
//        headers.setContentDisposition(ContentDisposition
//                .attachment()
//                .filename(invoice.getInvoiceNumber() + ".pdf")
//                .build());
//
//        return new ResponseEntity<>(file.getInputstream(), headers, HttpStatus.OK);
//    }
}
