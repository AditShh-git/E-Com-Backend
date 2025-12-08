package com.one.aim.repo;

import com.one.aim.bo.AddressBO;
import com.one.aim.bo.OrderBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
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
    //  SELLER DASHBOARD ANALYTICS
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
        SELECT COUNT(DISTINCT o.id)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
    """)
    Long countBySellerId(@Param("sellerId") Long sellerId);


    // Last Month Sales (MySQL native)
    @Query(value = """
        SELECT SUM(oi.total_price)
        FROM order_item oi
        WHERE oi.seller_id = :sellerId
          AND oi.created_at >= NOW() - INTERVAL 1 MONTH
    """, nativeQuery = true)
    Long getLastMonthSalesBySeller(@Param("sellerId") Long sellerId);


    // Last Month Order Count
    @Query("""
        SELECT COUNT(DISTINCT o.id)
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
    List<Object[]> findRecentOrders(@Param("sellerId") Long sellerId);


    // Sales By Product Per Day
    @Query("""
        SELECT p.name, DATE(o.createdAt), SUM(o.totalAmount)
        FROM OrderBO o
        JOIN o.cartItems c
        JOIN c.product p
        WHERE p.seller.id = :sellerId
        GROUP BY p.name, DATE(o.createdAt)
        ORDER BY p.name ASC
    """)
    List<Object[]> getSalesByProductPerDay(@Param("sellerId") Long sellerId);


    // Today's Sales
    @Query("""
    SELECT SUM(oi.totalPrice)
    FROM OrderItemBO oi
    WHERE oi.sellerId = :sellerId
      AND DATE(oi.createdAt) = CURRENT_DATE
""")
    Double getTodaySales(@Param("sellerId") Long sellerId);




    // ==========================================================
    // ADDITIONAL SELLER ANALYTICS
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
    // ORDER VOLUME (for summary cards)
    // ==========================================================

    @Query("""
        SELECT COUNT(o)
        FROM OrderBO o
        WHERE o.createdAt BETWEEN :start AND :end
    """)
    Long getOrderVolume(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );



    // ==========================================================
    // ADMIN DASHBOARD
    // ==========================================================

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

    boolean existsByShippingAddress(AddressBO address);

    @Query("""
    SELECT SUM(oi.totalPrice)
    FROM OrderItemBO oi
    WHERE oi.sellerId = :sellerId
      AND oi.createdAt BETWEEN :start AND :end
""")
    Double getSalesBetween(
            @Param("sellerId") Long sellerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );




    // Total Revenue by Seller
    @Query("""
    SELECT SUM(oi.totalPrice)
    FROM OrderItemBO oi
    WHERE oi.sellerId = :sellerId
""")
    Long getTotalRevenueBySeller(@Param("sellerId") Long sellerId);


    @Query("""
    SELECT COUNT(DISTINCT o.user.id)
    FROM OrderBO o
    WHERE o.orderTime BETWEEN :start AND :end
""")
    Long getActiveUsers(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<OrderBO> findAllByUser_IdOrderByOrderTimeDesc(Long userId);



    // ========================
// CORRECT ORDER COUNT
// ========================
    @Query("""
    SELECT COUNT(DISTINCT oi.order.id)
    FROM OrderItemBO oi
    WHERE oi.sellerId = :sellerId
""")
    Long getSellerOrderCount(@Param("sellerId") Long sellerId);


    // ========================
// CORRECT TOP PRODUCTS
// ========================
    @Query("""
    SELECT oi.productName, SUM(oi.quantity)
    FROM OrderItemBO oi
    WHERE oi.sellerId = :sellerId
    GROUP BY oi.productName
    ORDER BY SUM(oi.quantity) DESC
""")
    List<Object[]> getTopProductsOfSeller(@Param("sellerId") Long sellerId);



    @Query(value = """
    SELECT oi.product_name, SUM(oi.quantity)
    FROM order_items oi
    WHERE oi.seller_id = :sellerId
    GROUP BY oi.product_name
    ORDER BY SUM(oi.quantity) DESC
""", nativeQuery = true)
    List<Object[]> getTopProducts(@Param("sellerId") Long sellerId);


    List<OrderBO> findAllByOrderByOrderTimeDesc();

    Page<OrderBO> findByOrderStatusIgnoreCase(String status, Pageable pageable);

    @Query("""
    SELECT DISTINCT o
    FROM OrderBO o
    JOIN o.orderItems oi
    WHERE oi.sellerId = :sellerId
    AND (:status IS NULL OR o.orderStatus = :status)
""")
    Page<OrderBO> findOrdersForSeller(
            Long sellerId,
            String status,
            Pageable pageable
    );



    @Query("""
    SELECT CASE WHEN COUNT(oi) > 0 THEN TRUE ELSE FALSE END
    FROM OrderItemBO oi
    WHERE oi.order.orderId = :orderId
    AND oi.product.seller.id = :sellerId
""")
    boolean sellerOwnsOrder(@Param("orderId") String orderId,
                            @Param("sellerId") Long sellerId);





}

