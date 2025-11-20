package com.one.aim.service;

import com.one.aim.bo.ChatRoomBo;
import com.one.aim.bo.UserASDBO;

public interface ChatRoomService {

    ChatRoomBo getOrCreateRoom(UserASDBO admin, UserASDBO participant);

    ChatRoomBo getById(Long roomId);
}
