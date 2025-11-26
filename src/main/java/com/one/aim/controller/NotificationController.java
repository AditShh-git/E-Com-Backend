package com.one.aim.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.bo.NotificationBO;
import com.one.aim.rq.NotificationRq;
import com.one.aim.service.NotificationService;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseErrorRs;
import com.one.vm.core.BaseRs;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
		
	 private final NotificationService notificationService;

	    /**
	     *  Send Notification
	     */
	    @PostMapping("/send")
	    public BaseRs sendNotification(@RequestBody NotificationRq req) {
	        try {
	            NotificationBO bo = notificationService.send(
	                    req.getReceiverId(),
	                    req.getRole(),
	                    req.getTitle(),
	                    req.getMessage()
	            );

	            BaseRs rs = new BaseRs();
	            rs.setStatus("SUCCESS");
	            rs.setData(new BaseDataRs("Notification sent successfully", bo));
	            return rs;

	        } catch (Exception e) {
	            BaseErrorRs err = new BaseErrorRs();
	            err.setCode("500");
	            err.setMessage("Failed to send notification: " + e.getMessage());

	            BaseRs rs = new BaseRs();
	            rs.setStatus("FAILURE");
	            rs.setError(err);
	            return rs;
	        }
	    }

	    /**
	     *  Get All Notifications of a User
	     */
	    @GetMapping("/{receiverId}/all")
	    public BaseRs getAllNotifications(@PathVariable String receiverId) {
	        try {
	            List<NotificationBO> list = notificationService.getAllNotifications(receiverId);

	            BaseRs rs = new BaseRs();
	            rs.setStatus("SUCCESS");
	            rs.setData(new BaseDataRs("All notifications fetched", list));
	            return rs;

	        } catch (Exception e) {
	            BaseErrorRs err = new BaseErrorRs();
	            err.setCode("500");
	            err.setMessage("Failed to fetch notifications: " + e.getMessage());

	            BaseRs rs = new BaseRs();
	            rs.setStatus("FAILURE");
	            rs.setError(err);
	            return rs;
	        }
	    }

	    /**
	     *  Get Only Unread Notifications
	     */
	    @GetMapping("/{receiverId}/unread")
	    public BaseRs getUnreadNotifications(@PathVariable String receiverId) {
	        try {
	            List<NotificationBO> list = notificationService.getUnreadNotifications(receiverId);

	            BaseRs rs = new BaseRs();
	            rs.setStatus("SUCCESS");
	            rs.setData(new BaseDataRs("Unread notifications fetched", list));
	            return rs;

	        } catch (Exception e) {
	            BaseErrorRs err = new BaseErrorRs();
	            err.setCode("500");
	            err.setMessage("Failed to fetch unread notifications: " + e.getMessage());

	            BaseRs rs = new BaseRs();
	            rs.setStatus("FAILURE");
	            rs.setError(err);
	            return rs;
	        }
	    }

	    /**
	     *  Get Unread Count for Navbar Badge
	     */
	    @GetMapping("/{receiverId}/unread-count")
	    public BaseRs getUnreadCount(@PathVariable String receiverId) {
	        try {
	            long count = notificationService.getUnreadCount(receiverId);

	            BaseRs rs = new BaseRs();
	            rs.setStatus("SUCCESS");
	            rs.setData(new BaseDataRs("Unread count fetched", count));
	            return rs;

	        } catch (Exception e) {
	            BaseErrorRs err = new BaseErrorRs();
	            err.setCode("500");
	            err.setMessage("Failed to fetch unread count: " + e.getMessage());

	            BaseRs rs = new BaseRs();
	            rs.setStatus("FAILURE");
	            rs.setError(err);
	            return rs;
	        }
	    }

	    /**
	     *  Mark a Notification as Read
	     */
	    @PutMapping("/{id}/read")
	    public BaseRs markAsRead(@PathVariable Long id) {
	        try {
	            notificationService.markAsRead(id);

	            BaseRs rs = new BaseRs();
	            rs.setStatus("SUCCESS");
	            rs.setData(new BaseDataRs("Notification marked as read"));
	            return rs;

	        } catch (IllegalArgumentException iae) {
	            BaseErrorRs err = new BaseErrorRs();
	            err.setCode("404");
	            err.setMessage(iae.getMessage());

	            BaseRs rs = new BaseRs();
	            rs.setStatus("FAILURE");
	            rs.setError(err);
	            return rs;

	        } catch (Exception e) {
	            BaseErrorRs err = new BaseErrorRs();
	            err.setCode("500");
	            err.setMessage("Failed to mark notification as read: " + e.getMessage());

	            BaseRs rs = new BaseRs();
	            rs.setStatus("FAILURE");
	            rs.setError(err);
	            return rs;
	        }
	    }
}