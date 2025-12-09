package com.one.aim.service;

import com.one.aim.bo.NotificationUserStatusBO;

import java.util.List;

//import com.one.aim.bo.NotificationBO;

public interface NotificationService {

    // Notify all Admins (System events)
    void notifyAdmins(
            String type,
            String title,
            String description,
            Long imageFileId,
            Long redirectRefId,
            String redirectUrl
    );

    // Notify specific user (Order events)
    void notifyUser(
            Long userId,
            String type,
            String title,
            String description,
            Long imageFileId,
            Long redirectRefId,
            String redirectUrl
    );

    // Notify all Users (Public Sale events)
    void notifyAllUsers(
            String type,
            String title,
            String description,
            Long imageFileId,
            Long redirectRefId,
            String redirectUrl
    );

    // Notify all Sellers (Seller registration/approval events)
    void notifyAllSellers(
            String type,
            String title,
            String description,
            Long imageFileId,
            Long redirectRefId,
            String redirectUrl
    );

    // Notify all (Users + Sellers)
    void notifyBroadcast(
            String type,
            String title,
            String description,
            Long imageFileId,
            Long redirectRefId,
            String redirectUrl
    );


    // Fetch unread notifications for current user
    List<NotificationUserStatusBO> getUnreadForUser(Long userId);

    // Fetch full notification list for current user
    List<NotificationUserStatusBO> getAllForUser(Long userId);

    // Mark notification (status row) as read
    void markAsRead(Long statusId);
}
