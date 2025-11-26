package com.one.aim.service;

import java.util.List;

import com.one.aim.bo.NotificationBO;

public interface NotificationService {
	
    // Send notification
    NotificationBO send(String receiverId, String role, String title, String message);

    // Get ALL notifications of a user
    List<NotificationBO> getAllNotifications(String receiverId);

    // Get only unread notifications
    List<NotificationBO> getUnreadNotifications(String receiverId);

    // Get unread count
    long getUnreadCount(String receiverId);

    // Mark a notification as read
    void markAsRead(Long id);

    // Helper methods (optional)
    void notifyAdmin(String title, String msg);

    void notifySeller(Long sellerId, String title, String msg);

    void notifyUser(Long userId, String title, String msg);
}
