package com.one.aim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.one.aim.bo.OrderNotification;

@Controller
@RequestMapping(value = "/api")
public class OrderNotificationController {
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyVendor(String vendorId, String message) {
        OrderNotification notification = new OrderNotification();
        notification.setVendorName(vendorId);
        notification.setMessage(message);

        // This will send message to /topic/vendor/{vendorId}
        messagingTemplate.convertAndSend("/topic/vendor/" + vendorId, notification);
    }

}
