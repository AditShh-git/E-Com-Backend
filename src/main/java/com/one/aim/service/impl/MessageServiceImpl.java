package com.one.aim.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.one.aim.bo.ChatRoomBo;
import com.one.aim.bo.MessageBo;
import com.one.aim.bo.UserASDBO;
import com.one.aim.repo.MessageRepo;
import com.one.aim.service.MessageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService{
	
	private final MessageRepo messageRepository;
	
	@Override
	public MessageBo sendMessage(UserASDBO sender, ChatRoomBo room, String messageContent) {

        MessageBo message = MessageBo.builder()
                .sender(sender)
                .chatRoomBo(room)
                .content(messageContent)
                .timestamp(LocalDateTime.now())
                .readStatus(false)
                .build();

        return messageRepository.save(message);
	}

	@Override
	public List<MessageBo> getMessages(ChatRoomBo room) {
		return messageRepository.findByChatRoomBo_OrderByTimestampAsc(room);
	}

	@Override
	public void markAsRead(ChatRoomBo room, UserASDBO receiver) {

        List<MessageBo> messages = messageRepository.findByChatRoomBo_OrderByTimestampAsc(room);

        for (MessageBo m : messages) {
            if (!m.getSender().getId().equals(receiver.getId())) {
                m.setReadStatus(true);
                messageRepository.save(m);
            }
        }
	}

	@Override
	public int unreadMessageCount(ChatRoomBo room, UserASDBO user) {

        int count = 0;

        List<MessageBo> messages = messageRepository.findByChatRoomBo_OrderByTimestampAsc(room);

        for (MessageBo msg : messages) {
            if (!msg.getSender().getId().equals(user.getId()) && !msg.isReadStatus()) {
                count++;
            }
        }

        return count;
	}

}
