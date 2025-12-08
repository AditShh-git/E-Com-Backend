package com.one.aim.repo;

import com.one.aim.bo.InvoiceBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepo extends JpaRepository<InvoiceBO, Long> {

    // Fetch a single invoice by public ORDER ID
    Optional<InvoiceBO> findByOrder_OrderId(String orderId);

    // Fetch all invoices for a specific USER (User Panel)
    List<InvoiceBO> findByUser_Id(Long userId);

    // Fetch all invoices that include products from a SELLER
    @Query("""
        SELECT DISTINCT inv 
        FROM InvoiceBO inv
        JOIN inv.order ord
        JOIN ord.orderItems oi
        WHERE oi.product.seller.id = :sellerDbId
    """)
    List<InvoiceBO> findInvoicesForSeller(Long sellerDbId);
}

