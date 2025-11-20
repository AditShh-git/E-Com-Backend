package com.one.aim.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.one.aim.bo.ChatRoomBo;
import com.one.aim.bo.UserASDBO;

public interface ChatRoomRepo extends JpaRepository<ChatRoomBo, Long>{
	Optional<ChatRoomBo> findByAdminAndParticipant(UserASDBO admin, UserASDBO participant);
}
