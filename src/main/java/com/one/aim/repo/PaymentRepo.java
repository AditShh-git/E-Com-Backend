package com.one.aim.repo;

import com.one.aim.bo.OrderBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.PaymentBO;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<PaymentBO, Long> {

    Optional<PaymentBO> findByRazorpayPaymentId(String razorpayPaymentId);
    List<PaymentBO> findByOrder(OrderBO order);



}
