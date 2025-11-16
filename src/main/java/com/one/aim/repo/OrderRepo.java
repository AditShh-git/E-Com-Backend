package com.one.aim.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.OrderBO;

@Repository
public interface OrderRepo extends JpaRepository<OrderBO, Long> {

	//List<OrderBO> findAllByUserid(Long userid);
	
	List<OrderBO> findByUser_Id(Long userId);
	
	OrderBO findByRazorpayorderid(String razorpayorderid);

    @Query("""
    SELECT o FROM OrderBO o
    JOIN o.cartItems c
    JOIN c.product p
    JOIN p.seller s
    WHERE s.id = :sellerId
""")
    List<OrderBO> findOrdersBySellerId(@Param("sellerId") Long sellerId);


    OrderBO findByInvoiceno(String invoiceno);



}