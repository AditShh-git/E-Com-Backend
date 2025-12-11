package com.one.aim.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.one.aim.bo.NotificationEventBO;
import com.one.aim.mapper.NotificationMapper;
import com.one.aim.rs.NotificationRS;
import com.one.aim.service.FileService;
import com.one.utils.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.one.aim.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final FileService fileService;

    private String getLoggedRole() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("USER");
    }

    private boolean isRoleAllowed(NotificationEventBO event, String role) {
        String target = event.getTargetRole();
        return target == null ||           // direct-user notification
                target.equals("ALL") ||     // broadcast
                target.equals(role);        // match role
    }

    // ============================================================
    // Only unread notifications for Bell Icon
    // ============================================================
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> myUnread() {

        Long userId = AuthUtils.getLoggedUserId();
        String role = getLoggedRole();

        var list = notificationService.getUnreadForUser(userId).stream()
                .filter(n -> isRoleAllowed(n.getEvent(), role))  // ROLE FILTER APPLIED 🚀
                .map(n -> NotificationMapper.map(n.getEvent(), n, fileService))
                .toList();

        if (list.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "You have no new notifications"));
        }
        return ResponseEntity.ok(list);
    }

    // ============================================================
    // All notifications (read + unread) → Dashboard List
    // ============================================================
    @GetMapping("/me/all")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> myAll() {

        Long userId = AuthUtils.getLoggedUserId();
        String role = getLoggedRole();

        var list = notificationService.getAllForUser(userId).stream()
                .filter(n -> isRoleAllowed(n.getEvent(), role)) // ROLE FILTER APPLIED 🚀
                .map(n -> NotificationMapper.map(n.getEvent(), n, fileService))
                .toList();

        return list.isEmpty()
                ? ResponseEntity.ok(Map.of("message", "No notifications found"))
                : ResponseEntity.ok(list);
    }

    // ============================================================
    // Mark ONE notification read
    // ============================================================
    @PutMapping("/{statusId}/read")
    public ResponseEntity<?> markRead(@PathVariable Long statusId) {
        notificationService.markAsRead(statusId);
        return ResponseEntity.ok("Marked as read");
    }

    // ============================================================
    // Mark ALL my notifications read
    // ============================================================
    @PutMapping("/me/read-all")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> markAllRead() {
        Long userId = AuthUtils.getLoggedUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    // ============================================================
    // Hide/Delete notification
    // ============================================================
    @DeleteMapping("/{statusId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteNotification(@PathVariable Long statusId) {
        Long userId = AuthUtils.getLoggedUserId();
        notificationService.hideNotification(statusId, userId);
        return ResponseEntity.ok("Notification removed");
    }
}
