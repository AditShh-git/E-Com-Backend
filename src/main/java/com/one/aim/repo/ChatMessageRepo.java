package com.one.aim.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.one.aim.bo.ChatMessageBo;

public interface ChatMessageRepo extends JpaRepository<ChatMessageBo, Long>{
	List<ChatMessageBo> findByConversationBo_IdOrderByCreatedAtAsc(Long conversationId);
}
