package com.one.aim.bo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class OrderBO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long totalAmount;

	private String paymentMethod;

	private LocalDateTime orderTime;

	private String orderStatus;

	// --- Creator fields (Only one should be non-null) ---

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserBO user;

	// SELLER , VENDOR, ADMIN
	@ElementCollection
	@CollectionTable(name = "order_cartempids", joinColumns = @JoinColumn(name = "order_id"))
	@Column(name = "cartempid")
	private List<Long> cartempids = new ArrayList<>();

	// --- If one order has multiple delivery addresses (e.g. for multiple carts)
	// ---

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "address_id", nullable = false)
	private AddressBO shippingAddress;

	// --- Ordered cart items ---

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "order_cart_items", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "cart_id"))
	private List<CartBO> cartItems;

	@ManyToOne
	@JoinColumn(name = "delivery_person_id")
	private DeliveryPersonBO deliveryPerson;

	private String deliverystatus; // CREATED, ASSIGNED, PICKED_UP, DELIVERED, PENDING

	private String paymentstatus; // CREATED, ASSIGNED, PICKED_UP, DELIVERED, PENDING

	private String razorpayorderid;

	private String invoiceno;

}
