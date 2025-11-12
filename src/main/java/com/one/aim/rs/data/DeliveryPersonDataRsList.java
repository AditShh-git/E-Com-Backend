package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.rs.DeliveryPersonRs;
import com.one.vm.core.BaseDataRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryPersonDataRsList extends BaseDataRs {

	private static final long serialVersionUID = 1L;

	private List<DeliveryPersonRs> deliveryPersonRsList;

	public DeliveryPersonDataRsList(String message) {
		super(message);
	}

	public DeliveryPersonDataRsList(String message, List<DeliveryPersonRs> deliveryPersonRsList) {
		super(message);
		this.deliveryPersonRsList = deliveryPersonRsList;
	}

}
