package com.one.aim.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.one.aim.bo.ChatConversationBo;

public interface ChatConversationRepo extends JpaRepository<ChatConversationBo, Long>{
	Optional<ChatConversationBo> findByRoomId(String roomId); // convenience to find conversation by participant ids/roles (optional)
}