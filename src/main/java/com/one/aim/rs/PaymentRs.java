package com.one.aim.rs;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PaymentRs implements Serializable {

	private static final long serialVersionUID = 1L;

	private String amount;

	private String paymentMethod;

	private String userId;

}