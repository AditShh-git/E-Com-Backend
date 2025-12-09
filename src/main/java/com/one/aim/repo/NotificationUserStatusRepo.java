package com.one.aim.repo;

import com.one.aim.bo.NotificationUserStatusBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationUserStatusRepo extends JpaRepository<NotificationUserStatusBO, Long> {

    List<NotificationUserStatusBO> findByUserIdAndIsHiddenFalseOrderByCreatedAtDesc(Long userId);

    List<NotificationUserStatusBO> findByUserIdAndIsReadFalseAndIsHiddenFalseOrderByCreatedAtDesc(Long userId);
}
