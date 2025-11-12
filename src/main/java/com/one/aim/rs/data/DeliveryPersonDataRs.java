package com.one.aim.rs.data;

import com.one.aim.rs.DeliveryPersonRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryPersonDataRs extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private DeliveryPersonRs deliveryPersonRs;

	public DeliveryPersonDataRs(String message) {
		super(message);
	}

	public DeliveryPersonDataRs(String message, DeliveryPersonRs deliveryPersonRs) {
		super(message);
		this.deliveryPersonRs = deliveryPersonRs;
	}

}
