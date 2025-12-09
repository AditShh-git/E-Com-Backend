package com.one.aim.repo;

import com.one.aim.bo.AdminBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepo extends JpaRepository<AdminBO, Long> {

    // Login by email
    Optional<AdminBO> findByEmail(String email);



    // Lookup by full name
    Optional<AdminBO> findByFullName(String fullName);

    // Authentication check
    AdminBO findByIdAndFullName(Long id, String fullName);

    // Email OR Full Name lookup
    Optional<AdminBO> findByEmailOrFullName(String email, String fullName);

    // Case-insensitive email lookup
    Optional<AdminBO> findByEmailIgnoreCase(String email);

    // Reset password token
    Optional<AdminBO> findByResetToken(String token);

    // Phone number exists check
    boolean existsByPhoneNo(String phoneNo);

    // Email verification token
    Optional<AdminBO> findByVerificationToken(String token);
}
