package com.one.aim.rs;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderMaxRs implements Serializable {

	private static final long serialVersionUID = 1L;

	private String docId;

	private Long totalAmount;

	// private String Pname;

	private String paymentMethod;

	private LocalDateTime orderTime;

	private UserRs user;

	private List<CartRs> orderedItems;

}
