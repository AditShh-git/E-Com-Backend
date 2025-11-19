package com.one.aim.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.SellerBO;

@Repository
public interface SellerRepo extends JpaRepository<SellerBO, Long> {


    // ADD THIS (minimal change)
    Optional<SellerBO> findByResetToken(String resetToken);


    //  Find by full name
    Optional<SellerBO> findByFullName(String fullName);

    //  Find by email (case-insensitive)
    Optional<SellerBO> findByEmailIgnoreCase(String email);

    //  Find by email (used for login)
    Optional<SellerBO> findByEmail(String email);

    //  Replaces old findByEmailOrUsername
    Optional<SellerBO> findByEmailOrFullName(String email, String fullName);

    //  Used for verifying logged-in identity
    SellerBO findByIdAndFullName(Long id, String fullName);

    //  Keep this for list retrievals
    List<SellerBO> findAllByIdIn(List<Long> ids);

    //  Find seller by email verification token
    Optional<SellerBO> findByVerificationToken(String token);

    boolean existsByPhoneNo(String phoneNo);


    boolean existsByGst(String gst);
    boolean existsByAdhaar(String adhaar);
    boolean existsByPanCard(String panCard);

    Optional<SellerBO> findBySellerId(String sellerId);

//    Page<SellerBO> findAll(Specification<SellerBO> spec, Pageable pageable);
}
