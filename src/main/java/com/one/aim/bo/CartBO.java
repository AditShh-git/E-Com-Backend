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
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String pname;

	private String description;

	private long price;

	private String category;

	private boolean varified;

	private boolean enabled = true;

	private int totalitem;

	private int solditem;

	private int offer;

	private Long cartempid;

	private String cartempname;

	// Cart Items relationship
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_cart_id")
	private UserBO userCart;

	// Wishlist Items relationship
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_wishlist_id")
	private UserBO userWishlist;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_addcart_id") // 🔥 fixed lowercase, consistent
	private UserBO userAddToCart;

//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	@JoinColumn(name = "cart_id")
//	private List<AttachmentBO> cartatts;
	@Lob
	@Column(name = "image", columnDefinition = "LONGBLOB")
	private byte[] image;
}
