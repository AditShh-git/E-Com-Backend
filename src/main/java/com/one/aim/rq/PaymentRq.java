package com.one.aim.rq;

import com.one.vm.core.BaseVM;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentRq extends BaseVM {

	private static final long serialVersionUID = 1L;

	private String amount;

	private String paymentMethod;

	
	private String userId;





}
