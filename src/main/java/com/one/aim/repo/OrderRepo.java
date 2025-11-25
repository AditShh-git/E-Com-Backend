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
    // SELLER — Orders Containing Seller Products
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
    //  SELLER DASHBOARD ANALYTICS (REQUIRED)
    // ==========================================================

    // Total Sales
    @Query("""
        SELECT SUM(o.totalAmount)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
    """)
    Long getTotalSalesBySeller(@Param("sellerId") Long sellerId);

    // Total Orders
    @Query("""
        SELECT COUNT(o)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
    """)
    Long countBySellerId(@Param("sellerId") Long sellerId);

    // Last Month Sales (NATIVE - MySQL)
    // Last Month Sales (Corrected)
    @Query(value = """
    SELECT COUNT(DISTINCT oi.order_id)
    FROM order_items oi
    WHERE oi.seller_id = :sellerId
      AND oi.created_at >= NOW() - INTERVAL 1 MONTH
""", nativeQuery = true)
    Long getLastMonthSalesBySeller(@Param("sellerId") Long sellerId);

    // Last Month Order Count
    @Query("""
        SELECT COUNT(o)
        FROM OrderBO o
        JOIN o.cartItems ci
        JOIN ci.product p
        WHERE p.seller.id = :sellerId
          AND o.createdAt >= :startDate
    """)
    Long getLastMonthOrderCountBySeller(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDateTime startDate
    );

    // Recent Orders
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
    List<Object[]> findRecentOrdersBySeller(@Param("sellerId") Long sellerId);

    // Sales By Product Per Day (MySQL version)
    @Query("""
        SELECT p.name, DAY(o.createdAt), SUM(o.totalAmount)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
        GROUP BY p.name, DAY(o.createdAt)
        ORDER BY p.name ASC
    """)
    List<Object[]> getSalesByProductPerDay(@Param("sellerId") Long sellerId);

    // Today's Sales
    @Query("""
        SELECT SUM(o.totalAmount)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
          AND DATE(o.createdAt) = CURRENT_DATE
    """)
    Double getTodaySales(@Param("sellerId") Long sellerId);

    // Sales Between Date Range
    @Query("""
        SELECT SUM(o.totalAmount)
        FROM OrderBO o
        JOIN o.cartItems ci
        JOIN ci.product p
        WHERE p.seller.id = :sellerId
          AND o.createdAt BETWEEN :start AND :end
    """)
    Double getSalesBetween(
            @Param("sellerId") Long sellerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    // ==========================================================
    // SELLER ANALYTICS (MISSING METHODS ADDED)
    // ==========================================================

    @Query("""
        SELECT COUNT(DISTINCT o.id)
        FROM OrderBO o
        JOIN o.cartItems ci
        JOIN ci.product p
        WHERE p.seller.id = :sellerId
          AND o.createdAt BETWEEN :start AND :end
    """)
    Long countSellerOrders(
            @Param("sellerId") Long sellerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COUNT(DISTINCT o.user.id)
        FROM OrderBO o
        JOIN o.cartItems ci
        JOIN ci.product p
        WHERE p.seller.id = :sellerId
          AND o.createdAt BETWEEN :start AND :end
    """)
    Long countSellerUniqueCustomers(
            @Param("sellerId") Long sellerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COUNT(DISTINCT o.user.id)
        FROM OrderBO o
        JOIN o.cartItems ci
        JOIN ci.product p
        WHERE p.seller.id = :sellerId
          AND o.createdAt BETWEEN :start AND :end
          AND o.user.id IN (
              SELECT o2.user.id
              FROM OrderBO o2
              JOIN o2.cartItems ci2
              JOIN ci2.product p2
              WHERE p2.seller.id = :sellerId
                AND o2.createdAt < :start
          )
    """)
    Long countSellerReturningCustomers(
            @Param("sellerId") Long sellerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


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
    // EXTRA
    // ==========================================================
    boolean existsByShippingAddress(AddressBO address);

    // ==========================
    // ADMIN DASHBOARD METHODS
    // ==========================

    @Query("""
    SELECT SUM(o.totalAmount)
    FROM OrderBO o
""")
    Long getTotalRevenue();

    @Query("""
    SELECT MONTH(o.createdAt), SUM(o.totalAmount)
    FROM OrderBO o
    WHERE o.createdAt >= :startDate
    GROUP BY MONTH(o.createdAt)
    ORDER BY MONTH(o.createdAt)
""")
    List<Object[]> getRevenueByMonth(@Param("startDate") LocalDateTime startDate);

    @Query("""
    SELECT o.orderStatus, COUNT(o)
    FROM OrderBO o
    GROUP BY o.orderStatus
""")
    List<Object[]> getOrderDistribution();

}
