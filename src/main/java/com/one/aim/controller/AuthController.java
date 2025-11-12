package com.one.aim.controller;

import com.one.aim.rq.LoginRq;
import com.one.aim.service.AuthService;
import com.one.vm.core.BaseRs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    // ============================================
    // ✅ LOGIN (shared for all roles)
    // ============================================
    @PostMapping("/signin")
    public ResponseEntity<BaseRs> signIn(@RequestBody LoginRq rq) throws Exception {
        log.debug("Executing [POST /api/auth/signin] for user: {}", rq.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(rq.getEmail(), rq.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("User '{}' successfully signed in", rq.getEmail());
        return ResponseEntity.ok(authService.signIn(authentication));
    }

    // ============================================
    // ✅ FORGOT PASSWORD (shared)
    // ============================================
    @PostMapping("/forgot-password")
    public ResponseEntity<BaseRs> forgotPassword(@RequestParam String email) {
        log.debug("Executing [POST /api/auth/forgot-password] for email: {}", email);
        BaseRs response = authService.forgotPassword(email);
        log.info("Password reset request processed for email: {}", email);
        return ResponseEntity.ok(response);
    }

    // ============================================
    // ✅ RESET PASSWORD (shared)
    // ============================================
    @PostMapping("/reset-password")
    public ResponseEntity<BaseRs> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        log.debug("Executing [POST /api/auth/reset-password] for token: {}", token);
        BaseRs response = authService.resetPassword(token, newPassword);
        log.info("Password reset attempted for token: {}", token);

        return ResponseEntity.ok(response);
    }

    // ============================================
    // ✅ LOGOUT (shared)
    // ============================================
    @PostMapping("/logout")
    public ResponseEntity<BaseRs> logout() throws Exception {
        log.debug("Executing [POST /api/auth/logout]");
        BaseRs response = authService.logout();
        log.info("User successfully logged out");
        return ResponseEntity.ok(response);
    }
}

