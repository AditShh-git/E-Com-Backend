package com.one.aim.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.UserBO;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserBO, Long> {

    Optional<UserBO> findByEmail(String email);

    Optional<UserBO> findByResetToken(String resetToken);

    Optional<UserBO> findByVerificationToken(String verificationToken);

    UserBO findByFullName(String fullName);

    UserBO findByIdAndFullName(Long id, String fullName);

    UserBO findByFullNameAndId(String fullName, Long id);


    Optional<Object> findByEmailIgnoreCase(String email);
}
