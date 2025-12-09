package com.one.aim.repo;

import com.one.aim.bo.NotificationEventBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationEventRepo extends JpaRepository<NotificationEventBO, Long> {
    List<NotificationEventBO> findByTargetRoleOrderByCreatedAtDesc(String targetRole);
}
