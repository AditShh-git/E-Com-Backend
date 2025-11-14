package com.one.aim.bo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product",
        indexes = {@Index(name = "idx_product_seller", columnList = "seller_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private Double price;

    private Integer stock;

    private boolean lowStock = false;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private SellerBO seller;

    //  Multiple image IDs (store FileBO IDs from FileService)
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_file_id")
    private List<Long> imageFileIds = new ArrayList<>();

    //  Optional category text (until CategoryBO is ready)
    private String categoryName;

    //  Unique slug or UUID for shareable links
    @Column(unique = true, nullable = false, updatable = false)
    private String slug;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (slug == null || slug.isEmpty()) {
            this.slug = UUID.randomUUID().toString();
        }
    }

    public void updateLowStock() {
        this.lowStock = (this.stock != null && this.stock <= 5);
    }

}
