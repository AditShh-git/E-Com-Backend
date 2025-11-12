package com.one.aim.rq;

import com.one.vm.core.BaseVM;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryPersonRq extends BaseVM {

	private static final long serialVersionUID = 1L;

	private String docId;

	private String name;

	private String phoneno;

	private String email;

	private String password;

	private String aadhar;

	private String pan;

	private String bikeno;

	private String driveLience;

	private String city;
}
