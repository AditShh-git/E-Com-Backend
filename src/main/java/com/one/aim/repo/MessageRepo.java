package com.one.aim.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.one.aim.bo.ChatRoomBo;
import com.one.aim.bo.MessageBo;

public interface MessageRepo extends JpaRepository<MessageBo, Long>{
    List<MessageBo> findByChatRoomBo_OrderByTimestampAsc(ChatRoomBo chatRoomBo);
}
