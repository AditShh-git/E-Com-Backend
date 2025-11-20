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

    // ==========================================================
    // BASIC FINDERS
    // ==========================================================

    // Find all orders of a user
    List<OrderBO> findByUser_Id(Long userId);

    // Find by Razorpay order ID
    OrderBO findByRazorpayorderid(String razorpayorderid);

    // Find by Invoice No
    OrderBO findByInvoiceno(String invoiceno);

    // Payment Integration – Razorpay Order ID
    Optional<OrderBO> findByOrderId(String orderId);


    // ==========================================================
    // SELLER — Orders that contain seller’s products
    // ==========================================================
    @Query("""
        SELECT o FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        JOIN p.seller s
        WHERE s.id = :sellerId
    """)
    List<OrderBO> findOrdersBySellerId(@Param("sellerId") Long sellerId);


    // ==========================================================
    // ADMIN DASHBOARD ANALYTICS
    // ==========================================================

    // Total order count
    long count();

    // Total Revenue (Your friend’s correct method)
    @Query("SELECT SUM(o.totalAmount) FROM OrderBO o")
    Long sumTotalAmount();

    // Duplicate of above → still keeping because your code uses it
    @Query("SELECT SUM(o.totalAmount) FROM OrderBO o")
    Long getTotalRevenue();

    // Revenue by Month
    @Query("""
        SELECT FUNCTION('MONTH', o.createdAt), SUM(o.totalAmount)
        FROM OrderBO o
        WHERE o.createdAt >= :lastMonths
        GROUP BY FUNCTION('MONTH', o.createdAt)
    """)
    List<Object[]> getRevenueByMonth(LocalDateTime lastMonths);

    // Product-wise order distribution
    @Query("""
        SELECT p.name, COUNT(o)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        GROUP BY p.name
    """)
    List<Object[]> getOrderDistribution();


    // ==========================================================
    // FRIEND’S METHOD (KEEP THIS)
    // ==========================================================
    List<OrderBO> findAllByUserIdOrderByOrderTimeDesc(Long userId);

    // 1️⃣ Order volume (last 30 days)
    @Query("""
        SELECT COUNT(o)
        FROM OrderBO o
        WHERE o.orderStatus = 'DELIVERED'
          AND o.orderTime BETWEEN :start AND :end
    """)
    Long getOrderVolume(LocalDateTime start, LocalDateTime end);

    // 2️⃣ Weekly active users
    @Query("""
        SELECT COUNT(DISTINCT o.user.id)
        FROM OrderBO o
        WHERE o.orderTime BETWEEN :start AND :end
    """)
    Long getActiveUsers(LocalDateTime start, LocalDateTime end);


    // Unique customers for seller
    @Query("""
           SELECT COUNT(DISTINCT o.user.id)
           FROM OrderBO o
           JOIN o.orderItems oi
           WHERE oi.sellerId = :sellerId
             AND o.createdAt BETWEEN :start AND :end
           """)
    Long countSellerUniqueCustomers(@Param("sellerId") Long sellerId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // Returning customers (native SQL)
    @Query(value = """
           SELECT COUNT(*) FROM (
               SELECT o.user_id, COUNT(o.id) AS cnt
               FROM orders o
               JOIN order_items oi ON oi.order_id = o.id
               WHERE oi.seller_id = :sellerId
                 AND o.created_at BETWEEN :start AND :end
               GROUP BY o.user_id
               HAVING COUNT(o.id) >= 2
           ) AS t
           """, nativeQuery = true)
    Long countSellerReturningCustomers(@Param("sellerId") Long sellerId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    // Total seller orders
    @Query("""
           SELECT COUNT(DISTINCT o.id)
           FROM OrderBO o
           JOIN o.orderItems oi
           WHERE oi.sellerId = :sellerId
             AND o.createdAt BETWEEN :start AND :end
           """)
    Long countSellerOrders(@Param("sellerId") Long sellerId,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);


}
