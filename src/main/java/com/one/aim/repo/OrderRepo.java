package com.one.aim.repo;

import com.one.aim.bo.AddressBO;
import com.one.aim.bo.OrderBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<OrderBO, Long> {

    // ==========================================================
    // BASIC FINDERS
    // ==========================================================

    List<OrderBO> findByUser_Id(Long userId);

    OrderBO findByRazorpayorderid(String razorpayorderid);

    OrderBO findByInvoiceno(String invoiceno);

    Optional<OrderBO> findByOrderId(String orderId);


    // ==========================================================
    // SELLER — ORDERS CONTAINING SELLER PRODUCTS
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

    long count(); // total orders

    @Query("SELECT SUM(o.totalAmount) FROM OrderBO o")
    Long sumTotalAmount();

    @Query("SELECT SUM(o.totalAmount) FROM OrderBO o")
    Long getTotalRevenue();

    @Query("""
        SELECT FUNCTION('MONTH', o.createdAt), SUM(o.totalAmount)
        FROM OrderBO o
        WHERE o.createdAt >= :lastMonths
        GROUP BY FUNCTION('MONTH', o.createdAt)
    """)
    List<Object[]> getRevenueByMonth(LocalDateTime lastMonths);

    @Query("""
        SELECT p.name, COUNT(o)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        GROUP BY p.name
    """)
    List<Object[]> getOrderDistribution();


    // ==========================================================
    // USER ORDER HISTORY
    // ==========================================================
    List<OrderBO> findAllByUserIdOrderByOrderTimeDesc(Long userId);

    @Query("""
        SELECT COUNT(o)
        FROM OrderBO o
        WHERE o.orderStatus = 'DELIVERED'
        AND o.orderTime BETWEEN :start AND :end
    """)
    Long getOrderVolume(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT COUNT(DISTINCT o.user.id)
        FROM OrderBO o
        WHERE o.orderTime BETWEEN :start AND :end
    """)
    Long getActiveUsers(LocalDateTime start, LocalDateTime end);


    // ==========================================================
    // SELLER ANALYTICS (using OrderItems)
    // ==========================================================
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


    // ==========================================================
    // EXTRA: Check if Address is linked
    // ==========================================================
    boolean existsByShippingAddress(AddressBO address);
}
