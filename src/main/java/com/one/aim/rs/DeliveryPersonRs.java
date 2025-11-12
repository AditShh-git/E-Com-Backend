package com.one.aim.rs;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryPersonRs {

	private Long DocId;

	private String name;

	private String phone;

	private String email;

	private List<OrderRs> orders;

	private String aadhar;

	private String pan;

	private String bikeno;

	private String driveLience;

	private String city;

}
