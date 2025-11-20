package com.one.aim.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.one.aim.bo.ChatRoomBo;
import com.one.aim.bo.MessageBo;
import com.one.aim.bo.UserASDBO;
import com.one.aim.repo.UserASDBORepo;
import com.one.aim.rq.ChatRoomRq;
import com.one.aim.rq.SendMessageRq;
import com.one.aim.rs.StandardRs;
import com.one.aim.service.ChatRoomService;
import com.one.aim.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
	
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final UserASDBORepo userRepository;
    
    //  CREATE OR GET CHAT ROOM
    @PostMapping("/room")
    public StandardRs createOrGetRoom(@RequestBody ChatRoomRq rq) {

        UserASDBO admin = userRepository.findById(rq.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        UserASDBO participant = userRepository.findById(rq.getParticipantId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        ChatRoomBo room = chatRoomService.getOrCreateRoom(admin, participant);

        return new StandardRs("Chat room created or fetched successfully", room);
    }

    //  SEND MESSAGE
    @PostMapping("/send")
    public StandardRs sendMessage(@RequestBody SendMessageRq rq) {

        ChatRoomBo room = chatRoomService.getById(rq.getRoomId());

        UserASDBO sender = userRepository.findById(rq.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        MessageBo sentMessage = messageService.sendMessage(sender, room, rq.getMessage());

        return new StandardRs("Message sent successfully", sentMessage);
    }
    
    //  GET CHAT MESSAGES
    @GetMapping("/messages/{roomId}")
    public StandardRs getMessages(@PathVariable Long roomId) {

        ChatRoomBo room = chatRoomService.getById(roomId);

        List<MessageBo> messages = messageService.getMessages(room);

        return new StandardRs("Chat messages fetched successfully", messages);
    }
    
    //  UNREAD MESSAGE COUNT
    @GetMapping("/unread/{roomId}/{userId}")
    public StandardRs unreadCount(@PathVariable Long roomId, @PathVariable Long userId) {

        ChatRoomBo room = chatRoomService.getById(roomId);

        UserASDBO user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int unread = messageService.unreadMessageCount(room, user);

        return new StandardRs("Unread message count fetched", unread);
    }
    
    //  MARK AS READ
    @PostMapping("/read-all/{roomId}/{userId}")
    public StandardRs markMessagesRead(@PathVariable Long roomId, @PathVariable Long userId) {

        ChatRoomBo room = chatRoomService.getById(roomId);

        UserASDBO receiver = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        messageService.markAsRead(room, receiver);

        return new StandardRs("Messages marked as read", true);
    }
}
