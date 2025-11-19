package com.one.aim.repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.one.aim.bo.OrderBO;

@Repository
public interface OrderRepo extends JpaRepository<OrderBO, Long> {

    // Fetch by User ID
    List<OrderBO> findByUser_Id(Long userId);

    // Razorpay Order ID
    OrderBO findByRazorpayorderid(String razorpayorderid);

    // Seller → Get all orders which include seller’s products
    @Query("""
        SELECT o FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        JOIN p.seller s
        WHERE s.id = :sellerId
    """)
    List<OrderBO> findOrdersBySellerId(@Param("sellerId") Long sellerId);

    // Fetch by invoice no
    OrderBO findByInvoiceno(String invoiceno);

    // Payment integration: Find by OrderID
    Optional<OrderBO> findByOrderId(String orderId);


    // ======================================
    //            YOUR DASHBOARD QUERIES
    // ======================================

    // Order Count
    long count();

    // Looks like unused — keeping as you wrote
    Long sumTotalAmountBy();

    // Total Revenue
    @Query("SELECT SUM(o.totalAmount) FROM OrderBO o")
    Long getTotalRevenue();

    // Revenue by Month (Last X months)
    @Query("""
        SELECT FUNCTION('MONTH', o.createdAt), SUM(o.totalAmount)
        FROM OrderBO o
        WHERE o.createdAt >= :lastMonths
        GROUP BY FUNCTION('MONTH', o.createdAt)
    """)
    List<Object[]> getRevenueByMonth(LocalDateTime lastMonths);

    // Product-wise distribution
    @Query("""
        SELECT p.name, COUNT(o)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        GROUP BY p.name
    """)
    List<Object[]> getOrderDistribution();


    // ======================================
    //      FRIEND’S METHOD (Keep This)
    // ======================================
    List<OrderBO> findAllByUserIdOrderByOrderTimeDesc(Long userId);

}
