package com.one.aim.mapper;


import com.one.aim.bo.NotificationEventBO;
import com.one.aim.bo.NotificationUserStatusBO;
import com.one.aim.rs.NotificationRS;
import com.one.aim.service.FileService;

import java.time.Duration;
import java.time.LocalDateTime;

public class NotificationMapper {

    public static NotificationRS map(NotificationEventBO event,
                                     NotificationUserStatusBO status,
                                     FileService fileService) {

        String imageUrl = null;
        if (event.getImageFileId() != null) {
            imageUrl = fileService.getPublicFileUrl(event.getImageFileId());
        }

        return NotificationRS.builder()
                .id(status.getId()) // Status row ID
                .type(event.getType())
                .title(event.getTitle())
                .description(event.getDescription())
                .imageUrl(imageUrl)
                .redirectUrl(event.getRedirectUrl())
                .isRead(status.getIsRead())
                .timestamp(formatTimestamp(event.getCreatedAt()))
                .build();
    }

    private static String formatTimestamp(LocalDateTime createdAt) {
        Duration diff = Duration.between(createdAt, LocalDateTime.now());
        if (diff.toMinutes() < 1) return "Just now";
        if (diff.toMinutes() < 60) return diff.toMinutes() + " min ago";
        if (diff.toHours() < 24) return diff.toHours() + " hr ago";
        return diff.toDays() + " days ago";
    }
}
