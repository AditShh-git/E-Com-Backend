package com.one.aim.controller;

import com.one.aim.mapper.InvoiceMapper;
import com.one.aim.repo.FileRepo;
import com.one.aim.repo.InvoiceRepo;
import com.one.aim.rq.UpdateRq;
import com.one.aim.rq.UserFilterRequest;
import com.one.aim.rs.InvoiceRs;
import com.one.aim.service.InvoiceService;
import com.one.aim.service.UserService;
import com.one.utils.AuthUtils;
import com.one.vm.core.BaseRs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.one.aim.rq.UserRq;

import java.util.List;

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
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserRq rq) throws Exception {
        log.debug("Executing [POST /api/user/signup]");
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
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() throws Exception {
        log.debug("Executing [GET /api/user/all]");
        return ResponseEntity.ok(userService.retrieveAllUser());
    }

    // ===========================================================
    // DELETE USER BY ID (ADMIN ONLY)
    // ===========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) throws Exception {
        log.debug("Executing [DELETE /api/user/{}]", id);
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    // ===========================================================
    // UPDATE USER PROFILE  (USER ONLY)
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

    // =====================================================================
    //  USER → Get invoices for logged-in user
    // =====================================================================
    @GetMapping("/invoice/all")
    public List<InvoiceRs> getUserInvoices() {

        String role = AuthUtils.findLoggedInUser().getRoll();
        Long userId = AuthUtils.findLoggedInUser().getDocId();

        if (!"USER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Not allowed.");
        }

        return invoiceService.getInvoicesForUser(userId)
                .stream()
                .map(InvoiceMapper::toDto)
                .toList();
    }

    // =====================================================================
    // DELETE MY ACCOUNT (USER)
    // =====================================================================
    @DeleteMapping("/delete/me")
    public ResponseEntity<?> deleteMyAccount() throws Exception {
        return new ResponseEntity<>(userService.deleteMyAccount(), HttpStatus.OK);
    }

}
