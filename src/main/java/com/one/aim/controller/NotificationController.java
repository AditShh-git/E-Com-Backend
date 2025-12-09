package com.one.aim.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.one.aim.mapper.NotificationMapper;
import com.one.aim.rs.NotificationRS;
import com.one.aim.service.FileService;
import com.one.utils.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final FileService fileService;

    // ============================================================
    // Bell icon → only unread notifications
    // ============================================================
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> myUnread() {

        Long userId = AuthUtils.getLoggedUserId();

        var list = notificationService.getUnreadForUser(userId).stream()
                .map(status -> NotificationMapper.map(status.getEvent(), status, fileService))
                .toList();

        if (list.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "You have no new notifications"));
        }

        return ResponseEntity.ok(list);
    }



    // ============================================================
    // Notification list page → read + unread
    // ============================================================
    @GetMapping("/me/all")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> myAll() {

        Long userId = AuthUtils.getLoggedUserId();

        var list = notificationService.getAllForUser(userId).stream()
                .map(status -> NotificationMapper.map(status.getEvent(), status, fileService))
                .toList();

        if (list.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No notifications found"));
        }

        return ResponseEntity.ok(list);
    }



    // ============================================================
    // Mark notification as read (status row id)
    // ============================================================
    @PutMapping("/{statusId}/read")
    public ResponseEntity<?> markRead(@PathVariable Long statusId) {
        notificationService.markAsRead(statusId);
        return ResponseEntity.ok("Marked as read");
    }
}
