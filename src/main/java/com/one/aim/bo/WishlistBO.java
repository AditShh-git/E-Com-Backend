package com.one.aim.bo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wishlist")
public class WishlistBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserBO user;

    @ManyToOne
    private ProductBO product;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
