package com.one.aim.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.one.aim.bo.DeliveryPersonBO;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryPersonRepo extends JpaRepository<DeliveryPersonBO, Long> {

    // ✅ Find by email (used for login)
    Optional<DeliveryPersonBO> findByEmail(String email);

    // ✅ Replaces old findByEmailOrUsername
    Optional<DeliveryPersonBO> findByEmailOrFullName(String email, String fullName);

    // ✅ If you need it for authorization or identity verification
    DeliveryPersonBO findByIdAndFullName(Long id, String fullName);

    // ⚙️ Example for filtering (if needed in future)
    // List<DeliveryPersonBO> findByCityAndStatus(String city, String status);
}
