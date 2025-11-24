package com.one.aim.repo;

import com.one.aim.bo.InvoiceBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepo extends JpaRepository<InvoiceBO, Long> {

//    Optional<InvoiceBO> findByOrder_Id(String orderId);

    Optional<InvoiceBO> findByOrder_OrderId(String orderId);

//    List<InvoiceBO> findAllBySeller_Id(Long sellerId);
//    List<InvoiceBO> findAllBySeller_Id(Long sellerId);
//    List<InvoiceBO> findAllByUser_Id(Long userId);

}
