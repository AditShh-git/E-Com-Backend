package com.one.aim.bo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_roomsBo")
@Getter
@Setter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomBo {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Admin Participant
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private UserASDBO admin;

    // Other Participant (Seller or Delivery)
    @ManyToOne
    @JoinColumn(name = "participant_id")
    private UserASDBO participant;
}
