package com.one.aim.repo;

import com.one.aim.bo.OrderItemBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemBORepo extends JpaRepository<OrderItemBO, Long> {

    //  Get total revenue for a date range
    @Query("""
        SELECT COALESCE(SUM(oi.totalPrice), 0)
        FROM OrderItemBO oi
        JOIN oi.order o
        WHERE o.orderTime BETWEEN :start AND :end
          AND o.orderStatus = 'DELIVERED'
    """)
    Long getTotalRevenue(LocalDateTime start, LocalDateTime end);

    //  Weekly revenue for (Week 1–4)
    @Query("""
        SELECT COALESCE(SUM(oi.totalPrice), 0)
        FROM OrderItemBO oi
        JOIN oi.order o
        WHERE o.orderTime BETWEEN :start AND :end
          AND o.orderStatus = 'DELIVERED'
    """)
    Long getRevenueBetween(LocalDateTime start, LocalDateTime end);

    //  Top-selling product (highest revenue)
    @Query("""
        SELECT oi.productName, SUM(oi.totalPrice)
        FROM OrderItemBO oi
        JOIN oi.order o
        WHERE o.orderTime BETWEEN :start AND :end
          AND o.orderStatus = 'DELIVERED'
        GROUP BY oi.productName
        ORDER BY SUM(oi.totalPrice) DESC
    """)
    List<Object[]> getTopSellingProduct(LocalDateTime start, LocalDateTime end);

    //  Sales table (Product, Category, Seller, Revenue)
    @Query("""
        SELECT 
            oi.productName,
            oi.productCategory,
            oi.sellerId,
            SUM(oi.totalPrice)
        FROM OrderItemBO oi
        JOIN oi.order o
        WHERE o.orderTime BETWEEN :start AND :end
          AND o.orderStatus = 'DELIVERED'
        GROUP BY oi.productName, oi.productCategory, oi.sellerId
        ORDER BY SUM(oi.totalPrice) DESC
    """)
    List<Object[]> getSalesTable(LocalDateTime start, LocalDateTime end);


    // Total seller revenue in date range
    @Query("""
           SELECT COALESCE(SUM(oi.totalPrice), 0)
           FROM OrderItemBO oi
           WHERE oi.sellerId = :sellerId
             AND oi.createdAt BETWEEN :start AND :end
           """)
    Long getSellerTotalRevenue(@Param("sellerId") Long sellerId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    // Weekly revenue
    @Query("""
           SELECT COALESCE(SUM(oi.totalPrice), 0)
           FROM OrderItemBO oi
           WHERE oi.sellerId = :sellerId
             AND oi.createdAt BETWEEN :start AND :end
           """)
    Long getSellerRevenueBetween(@Param("sellerId") Long sellerId,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    // Product performance (last 30 days)
    @Query("""
           SELECT oi.productName,
                  oi.productCategory,
                  SUM(oi.quantity) AS totalQty,
                  SUM(oi.totalPrice) AS totalRevenue
           FROM OrderItemBO oi
           WHERE oi.sellerId = :sellerId
             AND oi.createdAt BETWEEN :start AND :end
           GROUP BY oi.productName, oi.productCategory
           ORDER BY totalRevenue DESC
           """)
    List<Object[]> getSellerProductPerformance(@Param("sellerId") Long sellerId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);





    // Monthly revenue for full year (Jan–Dec)
    @Query("""
           SELECT 
               MONTH(oi.createdAt) AS month,
               SUM(oi.totalPrice)  AS revenue
           FROM OrderItemBO oi
           WHERE oi.sellerId = :sellerId
             AND YEAR(oi.createdAt) = :year
           GROUP BY MONTH(oi.createdAt)
           ORDER BY MONTH(oi.createdAt)
           """)
    List<Object[]> getSellerMonthlyRevenue(@Param("sellerId") Long sellerId,
                                           @Param("year") int year);


}

