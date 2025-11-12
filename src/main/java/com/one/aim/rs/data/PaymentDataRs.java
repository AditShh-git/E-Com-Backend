package com.one.aim.rs.data;

import com.one.aim.rs.PaymentRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDataRs extends BaseDataRs {
	private static final long serialVersionUID = 1L;

	private PaymentRs payment;

	public PaymentDataRs(String message) {
		super(message);
	}

	public PaymentDataRs(String message, PaymentRs payment) {
		super(message);
		this.payment = payment;
	}

}
