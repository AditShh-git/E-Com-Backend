package com.one.aim.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.one.aim.bo.NotificationBO;
import com.one.aim.repo.NotificationRepo;
import com.one.aim.service.NotificationService;
import com.one.vm.utils.EmailUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
	
	 private final NotificationRepo notificationRepo;
	    private final EmailUtil emailUtil;

	    /**
	     * Send Notification
	     */
	    @Override
	    public NotificationBO send(String receiverId, String role, String title, String message) {

	        NotificationBO obj = new NotificationBO();
	        obj.setReceiverId(receiverId);
	        obj.setRole(role);
	        obj.setTitle(title);
	        obj.setMessage(message);

	        notificationRepo.save(obj);

	        // Send email notification
	        emailUtil.sendEmail(receiverId, title, message);

	        return obj;
	    }

	    /**
	     * Get All Notifications
	     */
	    @Override
	    public List<NotificationBO> getAllNotifications(String receiverId) {
	        return notificationRepo.findByReceiverIdOrderByCreatedAtDesc(receiverId);
	    }

	    /**
	     * Get Only Unread Notifications
	     */
	    @Override
	    public List<NotificationBO> getUnreadNotifications(String receiverId) {
	        return notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(receiverId);
	    }

	    /**
	     * Get Unread Count
	     */
	    @Override
	    public long getUnreadCount(String receiverId) {
	        return notificationRepo.countByReceiverIdAndIsReadFalse(receiverId);
	    }

	    /**
	     * Mark as read
	     */
	    @Override
	    public void markAsRead(Long id) {
	        NotificationBO bo = notificationRepo.findById(id)
	                .orElseThrow(() -> new RuntimeException("Notification not found"));

	        bo.setRead(true);
	        notificationRepo.save(bo);
	    }

	    // Helper methods for auto notifications:

	    public void notifyAdmin(String title, String msg) {
	        send("ADMIN", "ADMIN", title, msg);
	    }

	    public void notifySeller(Long sellerId, String title, String msg) {
	        send(sellerId.toString(), "SELLER", title, msg);
	    }

	    public void notifyUser(Long userId, String title, String msg) {
	        send(userId.toString(), "USER", title, msg);
	    }

}
