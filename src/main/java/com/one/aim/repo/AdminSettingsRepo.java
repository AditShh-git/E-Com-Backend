package com.one.aim.repo;

import com.one.aim.bo.AdminSettingsBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminSettingsRepo extends JpaRepository<AdminSettingsBO, Long> {

    Optional<AdminSettingsBO> findByKey(String key);
}
