package com.one.aim.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.AdminBO;

import java.util.Optional;

@Repository
public interface AdminRepo extends JpaRepository<AdminBO, Long> {

    //  Find by email (for login)
    Optional<AdminBO> findByEmail(String email);

    //  Find by full name (display or lookup)
    Optional<AdminBO> findByFullName(String fullName);

    //  Used for authentication checks (id + full name)
    AdminBO findByIdAndFullName(Long id, String fullName);

    //  Replaces old findByEmailOrUsername
    Optional<AdminBO> findByEmailOrFullName(String email, String fullName);

    Optional<AdminBO> findByEmailIgnoreCase(String email);

    Optional<AdminBO> findByResetToken(String token);

    boolean existsByPhoneNo(String phoneNo);

    Optional<AdminBO> findByVerificationToken(String token);

//    Optional<AdminBO> findByVerificationToken(String token);
}
