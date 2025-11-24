package com.one.aim.repo;

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

    // User orders
    List<OrderBO> findByUser_Id(Long userId);

    // Razorpay order lookup
    OrderBO findByRazorpayorderid(String razorpayorderid);

    // Seller → Orders
    @Query("""
        SELECT o FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        JOIN p.seller s
        WHERE s.id = :sellerId
    """)
    List<OrderBO> findOrdersBySellerId(@Param("sellerId") Long sellerId);

    // Invoice lookup
    OrderBO findByInvoiceno(String invoiceno);

    // From fix/invoice-seller-security branch
    Optional<OrderBO> findByOrderId(String orderId);

    // -------------------------------------------------------------
    // SELLER ANALYTICS + DASHBOARD QUERIES
    // -------------------------------------------------------------

    // Total sales
    @Query("""
        SELECT SUM(o.totalAmount)
        FROM OrderBO o 
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
    """)
    Long getTotalSalesBySeller(@Param("sellerId") String sellerId);

    // Total orders count
    @Query("""
        SELECT COUNT(o)
        FROM OrderBO o 
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
    """)
    Long countBySellerId(@Param("sellerId") String sellerId);

    // Last month sales count (native)
    @Query(value = """
        SELECT COUNT(o.id)
        FROM orders o
        JOIN cart_items ci ON ci.order_id = o.id
        JOIN product p ON p.id = ci.product_id
        WHERE p.seller_id = :sellerId
        AND o.created_at >= NOW() - INTERVAL 1 MONTH
    """, nativeQuery = true)
    Long getLastMonthSalesBySeller(@Param("sellerId") String sellerId);

    // Last month orders
    @Query("""
        SELECT COUNT(o)
        FROM OrderBO o
        JOIN o.cartItems ci
        JOIN ci.product p
        WHERE p.seller.id = :sellerId
        AND o.createdAt >= :startDate
    """)
    Long getLastMonthOrderCountBySeller(
            @Param("sellerId") String sellerId,
            @Param("startDate") LocalDateTime startDate
    );

    // Recent Orders (limit 10 manually)
    @Query("""
        SELECT o.id, u.fullName, o.createdAt, o.orderStatus, o.totalAmount
        FROM OrderBO o
        JOIN o.user u
        WHERE o.id IN (
            SELECT DISTINCT o2.id
            FROM OrderBO o2
            JOIN o2.cartItems ci
            JOIN ci.product p
            WHERE p.seller.id = :sellerId
        )
        ORDER BY o.createdAt DESC
    """)
    List<Object[]> findRecentOrdersBySeller(@Param("sellerId") String sellerId);

    // Sales by Product per Day
    @Query("""
        SELECT p.name, TO_CHAR(o.createdAt, 'DD'), SUM(o.totalAmount)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
        GROUP BY p.name, TO_CHAR(o.createdAt, 'DD')
        ORDER BY p.name ASC
    """)
    List<Object[]> getSalesByProductPerDay(@Param("sellerId") String sellerId);

    // Today sales
    @Query("""
        SELECT SUM(o.totalAmount)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
        AND DATE(o.createdAt) = CURRENT_DATE
    """)
    Double getTodaySales(@Param("sellerId") String sellerId);

    // Yesterday sales
    @Query("""
        SELECT SUM(o.totalAmount)
        FROM OrderBO o
        JOIN o.cartItems ci
        JOIN ci.product p
        WHERE p.seller.id = :sellerId
        AND o.createdAt BETWEEN :start AND :end
    """)
    Double getSalesBetween(
            @Param("sellerId") String sellerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
