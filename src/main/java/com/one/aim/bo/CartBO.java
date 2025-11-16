package com.one.aim.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart")
public class CartBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Product linked at time of add-to-cart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductBO product;

    // snapshot details
    private String pname;
    private long price;

    private int quantity = 1;
    private boolean enabled = true;

    // seller reference for invoice
    private Long sellerId;

    // The user who has this cart in their AddToCart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_addcart_id")
    private UserBO userAddToCart;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;
}
