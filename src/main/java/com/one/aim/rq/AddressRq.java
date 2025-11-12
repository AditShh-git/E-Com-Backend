package com.one.aim.rq;

import com.one.vm.core.BaseVM;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddressRq extends BaseVM {

	private static final long serialVersionUID = 1L;

	private String fullName;

	private String street;

	private String city;

	private String state;

	private String zip;

	private String country;

	private String phone;

}
