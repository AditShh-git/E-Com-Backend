package com.one.aim.service;

import java.util.List;

import com.one.aim.bo.ChatRoomBo;
import com.one.aim.bo.MessageBo;
import com.one.aim.bo.UserASDBO;

public interface MessageService {

    MessageBo sendMessage(UserASDBO sender, ChatRoomBo room, String messageContent);

    List<MessageBo> getMessages(ChatRoomBo room);

    void markAsRead(ChatRoomBo room, UserASDBO receiver);

    int unreadMessageCount(ChatRoomBo room, UserASDBO user);
}
