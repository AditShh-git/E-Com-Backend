package com.one.aim.rq;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomRq {
    private Long adminId;
    private Long participantId; // seller or delivery person
}
