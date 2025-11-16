package com.one.aim.bo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One invoice belongs to one order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderBO order;

    // The user who placed the order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserBO user;

    //  REMOVED — no single seller for multi-seller cart
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "seller_id")
    // private SellerBO seller;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    // PDF stored in FileService / DB
    @Column(name = "invoice_file_id", nullable = false)
    private Long invoiceFileId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
