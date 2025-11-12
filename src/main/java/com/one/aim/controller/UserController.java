package com.one.aim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // ============================================
    // ✅ REGISTER / SIGN-UP (normal users)
    // ============================================
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(@ModelAttribute UserRq rq,
                                          @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

        log.debug("Executing [POST /api/user/signup]");

        if (file != null && !file.isEmpty()) {
            rq.setImage(file); // keep MultipartFile
        }

        log.info("Processing registration for email: {}", rq.getEmail());
        return ResponseEntity.ok(userService.saveUser(rq));
    }

    // ============================================
    // ✅ GET CURRENT USER PROFILE
    // ============================================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() throws Exception {
        log.debug("Executing [GET /api/user/me]");
        ResponseEntity<?> response = ResponseEntity.ok(userService.retrieveUser());
        log.info("User profile retrieved successfully");
        return response;
    }

    // ============================================
    // ✅ GET ALL USERS (Admin only)
    // ============================================
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() throws Exception {
        log.debug("Executing [GET /api/user/all]");
        ResponseEntity<?> response = ResponseEntity.ok(userService.retrieveAllUser());
        log.info("All user records retrieved successfully");
        return response;
    }

    // ============================================
    // ✅ DELETE USER (Admin only)
    // ============================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) throws Exception {
        log.debug("Executing [DELETE /api/user/{}]", id);
        ResponseEntity<?> response = ResponseEntity.ok(userService.deleteUser(id));
        log.info("User deleted successfully: {}", id);
        return response;
    }
}
