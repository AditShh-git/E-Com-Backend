package com.one.aim.repo;

import com.one.aim.bo.CouponBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepo extends JpaRepository<CouponBO,Long> {

    Optional<CouponBO> findByCode(String code);

}
