package com.one.aim.controller;

import java.util.List;
import java.util.Optional;

import com.one.aim.bo.SellerBO;
import com.one.aim.bo.UserBO;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.security.jwt.JwtUtils;
import com.one.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.one.aim.bo.FileBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.service.FileService;
import com.one.vm.core.BaseRs;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private final JwtUtils jwtUtils;

    @Value("${dt.file.allow-content-types}")
    private List<String> ALLOW_CONTENT_TYPES;

    // =========================================================
    // AUTHENTICATED FILE UPLOAD
    // =========================================================
    @PostMapping(value = "/auth/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing REST Service - [POST /auth/file/upload]");
        }

        if (!ALLOW_CONTENT_TYPES.contains(multipartFile.getContentType())) {
            log.error("Invalid Content Type - " + multipartFile.getContentType());
            throw new Exception(ErrorCodes.EC_ALLOWED_FILE_TYPES);
        }

        BaseRs baseRs = fileService.uploadFile(multipartFile);
        return ResponseEntity.ok(baseRs);
    }


    // =========================================================
    // DELETE FILE
    // =========================================================
    @DeleteMapping("/auth/file/{fileId}")
    public ResponseEntity<?> deleteFileById(@PathVariable String fileId) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing REST Service - [DELETE /auth/file/{fileId}]");
        }

        BaseRs baseRs = fileService.deleteFileById(fileId);
        return ResponseEntity.ok(baseRs);
    }


    // =========================================================
    // RAW FILE CONTENT (Not recommended for public use)
    // =========================================================
    @GetMapping("/auth/file/img/{fileId}")
    public ResponseEntity<?> getImg(@PathVariable String fileId) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Executing REST Service - [GET /auth/file/img/{fileId}]");
        }

        byte[] bytes = fileService.getContentFromGridFS(fileId);
        return ResponseEntity.ok(bytes);
    }


    // =========================================================
    // PUBLIC PRODUCT, BANNER, CATEGORY FILES
    // Accessible by USERS & SELLERS (NO AUTH REQUIRED)
    // =========================================================
    @GetMapping("/files/public/{id}/view")
    public ResponseEntity<byte[]> viewPublicFile(@PathVariable String id) throws Exception {

        log.info("PUBLIC FILE VIEW HIT: " + id);

        FileBO file = fileService.getFile(id);
        byte[] content = fileService.getContentFromGridFS(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContenttype()))
                .body(content);
    }


    // =========================================================
    // PRIVATE FILES (Profile images)
    // =========================================================
//    @GetMapping("/files/private/{id}/view")
//    public ResponseEntity<byte[]> viewPrivateFile(@PathVariable String id) throws Exception {
//
//        Long viewerId = AuthUtils.getLoggedUserId();
//        String role = AuthUtils.getLoggedUserRole();
//
//        if (viewerId == null || role == null) {
//            throw new RuntimeException("Unauthorized");
//        }
//
//        FileBO file = fileService.getFile(id);
//
//        // ADMIN → full access
//        if ("ADMIN".equalsIgnoreCase(role)) {
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(file.getContenttype()))
//                    .body(fileService.getContentFromGridFS(id));
//        }
//
//        // USER can only see their profile image
//        if ("USER".equalsIgnoreCase(role)) {
//            UserBO user = userRepo.findById(viewerId).orElse(null);
//
//            if (user != null && id.equals(String.valueOf(user.getImageFileId()))) {
//                return ResponseEntity.ok()
//                        .contentType(MediaType.parseMediaType(file.getContenttype()))
//                        .body(fileService.getContentFromGridFS(id));
//            }
//            throw new RuntimeException("Access Denied");
//        }
//
//        // SELLER can only see their profile image
//        if ("SELLER".equalsIgnoreCase(role)) {
//            SellerBO seller = sellerRepo.findById(viewerId).orElse(null);
//
//            if (seller != null && id.equals(String.valueOf(seller.getImageFileId()))) {
//                return ResponseEntity.ok()
//                        .contentType(MediaType.parseMediaType(file.getContenttype()))
//                        .body(fileService.getContentFromGridFS(id));
//            }
//            throw new RuntimeException("Access Denied");
//        }
//
//        throw new RuntimeException("Access Denied");
//    }

    @GetMapping("/files/private/{id}/view")
    public ResponseEntity<byte[]> viewPrivateFile(@PathVariable String id) throws Exception {

        FileBO file = fileService.getFile(id);
        byte[] content = fileService.getContentFromGridFS(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContenttype()))
                .body(content);
    }

}
