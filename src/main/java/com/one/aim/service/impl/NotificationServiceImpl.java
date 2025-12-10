package com.one.aim.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.one.aim.bo.*;
import com.one.aim.controller.NotificationWSController;
import com.one.aim.mapper.NotificationMapper;
import com.one.aim.repo.*;
import com.one.aim.service.FileService;
import org.springframework.stereotype.Service;

//import com.one.aim.bo.NotificationBO;
//import com.one.aim.repo.NotificationRepo;
import com.one.aim.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationEventRepo eventRepo;
    private final NotificationUserStatusRepo statusRepo;
    private final UserRepo userRepo;
    private final NotificationWSController wsController;
    private final FileService fileService;
    private final NotificationUserStatusRepo userStatusRepo;
    private static final int MAX_VISIBLE_PER_USER = 20;
    private static final int EXPIRY_DAYS = 30;
    private final AdminRepo adminRepo;
    private final SellerRepo sellerRepo;

    // =====================================================
    //                 SEND NOTIFICATIONS
    // =====================================================

    @Override
    public void notifyAdmins(String type, String title, String description,
                             Long imageFileId, Long redirectRefId, String redirectUrl) {

        NotificationEventBO event = saveEvent(type, title, description,
                imageFileId, redirectRefId, redirectUrl,
                "ADMIN"
        );

        List<AdminBO> admins = adminRepo.findAll();

        admins.forEach(a -> {
            NotificationUserStatusBO status = saveUserStatus(a.getId(), event);
            wsController.sendToUserWS(a.getId(),
                    NotificationMapper.map(event, status, fileService));
        });
    }

    @Override
    public void notifyUser(Long userId, String type, String title, String description,
                           Long imageFileId, Long redirectRefId, String redirectUrl) {

        NotificationEventBO event = saveEvent(type, title, description,
                imageFileId, redirectRefId, redirectUrl,
                null // user-specific, not role-based
        );

        NotificationUserStatusBO status = saveUserStatus(userId, event);

        wsController.sendToUserWS(userId,
                NotificationMapper.map(event, status, fileService));
    }

    @Override
    public void notifyAllUsers(String type, String title, String description,
                               Long imageFileId, Long redirectRefId, String redirectUrl) {

        NotificationEventBO event = saveEvent(type, title, description,
                imageFileId, redirectRefId, redirectUrl,
                "USER"
        );

        List<UserBO> users = userRepo.findAll();

        users.forEach(u -> {
            NotificationUserStatusBO status = saveUserStatus(u.getId(), event);
            wsController.sendToUserWS(u.getId(),
                    NotificationMapper.map(event, status, fileService));
        });
    }

    @Override
    public void notifyAllSellers(String type, String title, String description,
                                 Long imageFileId, Long redirectRefId, String redirectUrl) {

        NotificationEventBO event = saveEvent(type, title, description,
                imageFileId, redirectRefId, redirectUrl,
                "SELLER"
        );

        List<SellerBO> sellers = sellerRepo.findAll();

        sellers.forEach(s -> {
            NotificationUserStatusBO status = saveUserStatus(s.getId(), event);
            wsController.sendToUserWS(s.getId(),
                    NotificationMapper.map(event, status, fileService));
        });
    }

    @Override
    public void notifyBroadcast(String type, String title, String description,
                                Long imageFileId, Long redirectRefId, String redirectUrl) {

        NotificationEventBO event = saveEvent(type, title, description,
                imageFileId, redirectRefId, redirectUrl,
                "ALL"
        );

        List<UserBO> all = userRepo.findAll();

        all.forEach(u -> {
            NotificationUserStatusBO status = saveUserStatus(u.getId(), event);
            wsController.sendToUserWS(u.getId(),
                    NotificationMapper.map(event, status, fileService));
        });
    }


    // =====================================================
    //                 FETCH NOTIFICATIONS
    // =====================================================

    @Override
    public List<NotificationUserStatusBO> getUnreadForUser(Long userId) {
        return statusRepo
                .findByUserIdAndIsReadFalseAndIsHiddenFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<NotificationUserStatusBO> getAllForUser(Long userId) {
        return statusRepo
                .findByUserIdAndIsHiddenFalseOrderByCreatedAtDesc(userId);
    }


    // =====================================================
    //                    MARK AS READ
    // =====================================================

    @Override
    public void markAsRead(Long statusId) {
        statusRepo.findById(statusId).ifPresent(s -> {
            s.setIsRead(true);
            s.setReadAt(LocalDateTime.now());
            statusRepo.save(s);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<NotificationUserStatusBO> unreadNotifs =
                userStatusRepo.findByUserIdAndIsReadFalseAndIsHiddenFalseOrderByCreatedAtDesc(userId);

        if (unreadNotifs.isEmpty()) {
            return;
        }

        unreadNotifs.forEach(n -> n.setIsRead(true));
        userStatusRepo.saveAll(unreadNotifs);
    }

    @Override
    @Transactional
    public void hideNotification(Long statusId, Long userId) {
        var status = userStatusRepo.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!status.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized delete attempt");
        }

        status.setIsHidden(true);
        userStatusRepo.save(status);
    }



    // =====================================================
    //            PRIVATE: SAVE EVENT + STATUS
    // =====================================================

    private NotificationEventBO saveEvent(String type, String title, String description,
                                          Long imageFileId, Long redirectRefId,
                                          String redirectUrl, String targetRole) {

        NotificationEventBO event = NotificationEventBO.builder()
                .type(type)
                .title(title)
                .description(description)
                .imageFileId(imageFileId)
                .redirectRefId(redirectRefId)
                .redirectUrl(redirectUrl)
                .targetRole(targetRole)
                .expiryAt(LocalDateTime.now().plusDays(EXPIRY_DAYS))
                .build();

        return eventRepo.save(event);
    }

    private NotificationUserStatusBO saveUserStatus(Long userId, NotificationEventBO event) {

        NotificationUserStatusBO status = NotificationUserStatusBO.builder()
                .userId(userId)
                .event(event)
                .build();

        NotificationUserStatusBO saved = statusRepo.save(status);

        enforceUserRetention(userId);
        return saved;
    }


    // =====================================================
    //          RETENTION POLICY (Only Latest 20)
    // =====================================================

    private void enforceUserRetention(Long userId) {
        List<NotificationUserStatusBO> all =
                statusRepo.findByUserIdAndIsHiddenFalseOrderByCreatedAtDesc(userId);

        if (all.size() > MAX_VISIBLE_PER_USER) {
            all.subList(MAX_VISIBLE_PER_USER, all.size())
                    .forEach(s -> s.setIsHidden(true));
            statusRepo.saveAll(all);
        }
    }
}
