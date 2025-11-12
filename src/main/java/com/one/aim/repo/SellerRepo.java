package com.one.aim.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.SellerBO;

@Repository
public interface SellerRepo extends JpaRepository<SellerBO, Long> {

    // ✅ Find by full name
    Optional<SellerBO> findByFullName(String fullName);

    Optional<SellerBO> findByEmailIgnoreCase(String email);


    // ✅ Find by email (used for login)
    Optional<SellerBO> findByEmail(String email);

    // ✅ Replaces old findByEmailOrUsername
    Optional<SellerBO> findByEmailOrFullName(String email, String fullName);

    // ✅ Used for verifying logged-in identity
    SellerBO findByIdAndFullName(Long id, String fullName);

    // ✅ Keep this for list retrievals
    List<SellerBO> findAllByIdIn(List<Long> ids);

}
