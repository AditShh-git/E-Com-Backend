package com.one.aim.repo;

import com.one.aim.bo.UserActivityBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityRepo extends JpaRepository<UserActivityBO, Long> {

    List<UserActivityBO> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT ua FROM UserActivityBO ua ORDER BY ua.createdAt DESC")
    List<UserActivityBO> findAllOrderByCreatedAtDesc();

}
