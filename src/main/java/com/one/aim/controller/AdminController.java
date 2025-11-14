package com.one.aim.controller;

import com.one.aim.bo.AdminBO;
import com.one.aim.repo.AdminRepo;
import com.one.aim.rq.LoginRq;
import com.one.aim.service.AuthService;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping(value = "/api/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final AdminService adminService;   // You likely already have this
    private final AdminRepo adminRepo;           // To verify admin role


    // ============================================================
    // ADMIN SIGN-UP (REGISTER)
    // ============================================================
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseRs> registerAdmin(
            @ModelAttribute AdminRq rq,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws Exception {

        log.debug("Executing [POST /api/admin/signup]");

        if (file != null && !file.isEmpty()) {
            rq.setImage(file.getBytes());
        }

        log.info("Admin registration request for email: {}", rq.getEmail());

        BaseRs response = adminService.saveAdmin(rq);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // ADMIN SIGN-IN
    // ============================================================
    @PostMapping("/signin")
    public ResponseEntity<BaseRs> adminSignIn(@RequestBody LoginRq rq) throws Exception {

        log.debug("Executing [POST /api/admin/signin] for admin: {}", rq.getEmail());

        // Authenticate email + password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(rq.getEmail(), rq.getPassword())
        );

        // Load admin record
        AdminBO admin = adminRepo.findByEmail(rq.getEmail())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Only allow ADMIN role
        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseUtils.failure("EC_ACCESS_DENIED", "Admin access only"));
        }

        // Success — set auth into Spring Security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Return token + admin info
        return ResponseEntity.ok(authService.signIn(authentication));
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


    // ============================================================
    // ADMIN LOGOUT
    // ============================================================
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/logout")
    public ResponseEntity<BaseRs> adminLogout() throws Exception {
        log.debug("Executing [POST /api/admin/logout]");
        return ResponseEntity.ok(authService.logout());
    }
}
