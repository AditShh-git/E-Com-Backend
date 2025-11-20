package com.one.aim.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.one.aim.bo.UserASDBO;
import com.one.aim.constants.UserRole;

public interface UserASDBORepo extends JpaRepository<UserASDBO, Long>{

    Optional<UserASDBO> findByEmail(String email);

    List<UserASDBO> findByRole(UserRole role);
}
