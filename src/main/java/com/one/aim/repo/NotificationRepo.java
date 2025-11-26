package com.one.aim.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.one.aim.bo.NotificationBO;

public interface NotificationRepo extends JpaRepository<NotificationBO, Long>{
	 //List<NotificationBO> findByReceiverIdOrderByCreatedAtDesc(String receiverId);
	List<NotificationBO> findByReceiverIdOrderByCreatedAtDesc(String receiverId);

	List<NotificationBO> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(String receiverId);

	Long countByReceiverIdAndIsReadFalse(String receiverId);
}
