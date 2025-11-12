package com.one.aim.service;

import com.one.aim.rq.DeliveryPersonRq;
import com.one.vm.core.BaseRs;

public interface DeliveryPersonService {

	public BaseRs saveDeliveryPerson(DeliveryPersonRq rq);

	public BaseRs retrieveDeliveryPersons();

	public BaseRs retrieveDeliveryPersonById(String id);

}
