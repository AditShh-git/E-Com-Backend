package com.one.aim.controller;

import com.one.aim.constants.ErrorCodes;
import com.one.aim.rq.LoginRq;
import com.one.aim.rq.ResetPasswordRq;
import com.one.aim.rq.UserRq;
import com.one.aim.service.AuthService;
import com.one.aim.service.UserService;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    // ===========================================================
    // LOGIN (User + Seller + Admin)
    // ===========================================================
    @PostMapping("/signin")
    public ResponseEntity<BaseRs> signIn(@RequestBody LoginRq rq) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(rq.getEmail(), rq.getPassword())
            );

            return ResponseEntity.ok(authService.signIn(authentication));

        } catch (Exception e) {
            log.error("Error in signIn(): {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(ResponseUtils.failure("INVALID_CREDENTIALS"));
        }
    }

    // ===========================================================
    // VERIFY EMAIL (User + Seller + Admin)
    // ===========================================================
    @GetMapping("/verify-email")
    public ResponseEntity<BaseRs> verifyEmail(@RequestParam String token) {
        try {
            return ResponseEntity.ok(authService.verifyEmail(token));
        } catch (Exception e) {
            log.error("Error in verifyEmail(): {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ResponseUtils.failure("INTERNAL_ERROR"));
        }
    }

    // ===========================================================
    // FORGOT PASSWORD (User + Seller + Admin)
    // ===========================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<BaseRs> forgotPassword(@RequestParam String email) {
        try {
            return ResponseEntity.ok(authService.forgotPassword(email));
        } catch (Exception e) {
            log.error("Error in forgotPassword(): {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ResponseUtils.failure("INTERNAL_ERROR"));
        }
    }

    // ===========================================================
    // RESET PASSWORD
    // ===========================================================
    @PostMapping("/reset-password")
    public ResponseEntity<BaseRs> resetPassword(@RequestBody ResetPasswordRq rq) {
        try {
            return ResponseEntity.ok(authService.resetPassword(rq.getToken(), rq.getNewPassword()));
        } catch (Exception e) {
            log.error("Error in resetPassword(): {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ResponseUtils.failure("INTERNAL_ERROR"));
        }
    }

    // ===========================================================
    // RESEND VERIFICATION (User + Seller + Admin)
    // ===========================================================
    @PostMapping("/resend-verification")
    public ResponseEntity<BaseRs> resendVerification(@RequestParam String email) {
        try {
            return ResponseEntity.ok(authService.resendVerificationEmail(email));
        } catch (Exception e) {
            log.error("Error in resendVerification(): {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ResponseUtils.failure("INTERNAL_ERROR"));
        }
    }

    // ===========================================================
    // LOGOUT (User + Seller + Admin)
    // ===========================================================
    @PostMapping("/logout")
    public ResponseEntity<BaseRs> logout() {
        try {
            return ResponseEntity.ok(authService.logout());
        } catch (Exception e) {
            log.error("Error in logout(): {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ResponseUtils.failure("INTERNAL_ERROR"));
        }
    }
}
