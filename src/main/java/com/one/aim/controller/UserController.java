package com.one.aim.controller;

import com.one.aim.rq.UpdateRq;
import com.one.aim.service.AuthService;
import com.one.utils.AuthUtils;
import com.one.vm.core.BaseRs;
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
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    // ============================================
    //  REGISTER / SIGN-UP
    // ============================================
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

    // ============================================
    //  LOGIN
    // ============================================
    @PostMapping("/signin")
    public ResponseEntity<BaseRs> signIn(@RequestBody LoginRq rq) throws Exception {
        log.debug("Executing [POST /api/user/signin] for user: {}", rq.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(rq.getEmail(), rq.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("User '{}' successfully signed in", rq.getEmail());
        return ResponseEntity.ok(authService.signIn(authentication));
    }

    // ============================================
    //  CURRENT USER PROFILE
    // ============================================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() throws Exception {
        log.debug("Executing [GET /api/user/me]");
        return ResponseEntity.ok(userService.retrieveUser());
    }

    // ============================================
    //  ALL USERS (Admin)
    // ============================================
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() throws Exception {
        log.debug("Executing [GET /api/user/all]");
        return ResponseEntity.ok(userService.retrieveAllUser());
    }

    // ============================================
    //  DELETE USER (Admin)
    // ============================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) throws Exception {
        log.debug("Executing [DELETE /api/user/{}]", id);
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    // ============================================
    //  FORGOT PASSWORD
    // ============================================
    @PostMapping("/forgot-password")
    public ResponseEntity<BaseRs> forgotPassword(@RequestParam String email) {
        log.debug("Executing [POST /api/user/forgot-password]");
        return ResponseEntity.ok(authService.forgotPassword(email));
    }

    // ============================================
    //  RESET PASSWORD
    // ============================================
    @PostMapping("/reset-password")
    public ResponseEntity<BaseRs> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        log.debug("Executing [POST /api/user/reset-password]");
        return ResponseEntity.ok(authService.resetPassword(token, newPassword));
    }

    // ============================================
    //  LOGOUT
    // ============================================
    @PostMapping("/logout")
    public ResponseEntity<BaseRs> logout() throws Exception {
        log.debug("Executing [POST /api/user/logout]");
        return ResponseEntity.ok(authService.logout());
    }

    // ============================================
//  UPDATE USER PROFILE
// ============================================
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

        // **THE FIX HERE** → use Spring Security directly
        String loggedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        BaseRs response = userService.updateUserProfile(loggedEmail, rq);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<BaseRs> verifyEmail(@RequestParam("token") String token) {
        log.debug("Email verification attempt with token");
        return ResponseEntity.ok(userService.verifyEmail(token));
    }
}
