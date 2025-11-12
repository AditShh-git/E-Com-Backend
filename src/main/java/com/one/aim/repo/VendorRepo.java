package com.one.aim.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.SellerBO;
import com.one.aim.bo.VendorBO;

@Repository
public interface VendorRepo extends JpaRepository<VendorBO, Long> {

    // ✅ Find by email (used for login)
    Optional<VendorBO> findByEmail(String email);

    // ✅ Replace old "findByUsername"
    VendorBO findByFullName(String fullName);

    // ✅ Replace "findByEmailOrUsername" → fullName-based query
    Optional<VendorBO> findByEmailOrFullName(String email, String fullName);

    // ✅ Used for login or validation
    VendorBO findByIdAndFullName(Long id, String fullName);

    // ✅ Keep as-is for multiple IDs
    List<VendorBO> findAllByIdIn(List<Long> ids);

}