package com.one.aim.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.one.aim.bo.ChatRoomBo;
import com.one.aim.bo.UserASDBO;
import com.one.aim.repo.ChatRoomRepo;
import com.one.aim.service.ChatRoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService{

	private final ChatRoomRepo chatRoomRepository;
	
	@Override
	public ChatRoomBo getOrCreateRoom(UserASDBO admin, UserASDBO participant) {

        Optional<ChatRoomBo> room = chatRoomRepository.findByAdminAndParticipant(admin, participant);

        if (room.isPresent()) {
            return room.get();
        }

        ChatRoomBo newRoom = ChatRoomBo.builder()
                .admin(admin)
                .participant(participant)
                .build();

        return chatRoomRepository.save(newRoom);
	}

	@Override
	public ChatRoomBo getById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
	}

}
