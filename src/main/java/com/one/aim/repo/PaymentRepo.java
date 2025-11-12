package com.one.aim.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.PaymentBO;

@Repository
public interface PaymentRepo extends JpaRepository<PaymentBO, Long> {

}
