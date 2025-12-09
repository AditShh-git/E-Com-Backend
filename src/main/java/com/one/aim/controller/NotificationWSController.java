package com.one.aim.controller;

import com.one.aim.rs.NotificationRS;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NotificationWSController {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUserWS(Long userId, NotificationRS dto) {
        messagingTemplate.convertAndSend("/topic/user-notifications/" + userId, dto);
    }
}

