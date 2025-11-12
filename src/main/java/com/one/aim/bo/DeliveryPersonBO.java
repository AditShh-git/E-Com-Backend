package com.one.aim.bo;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "delivery_person")
public class DeliveryPersonBO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String fullName;

	private String phoneNo;

	private String status; // AVAILABLE, ON_DELIVERY, OFFLINE

	private String email;

	private String aadharNo;

	private String pan;

	private String bikeNo;

	private String drivingLicence;

	private String city;

	@OneToMany(mappedBy = "deliveryPerson")
	private List<OrderBO> orders;

	private String password;
}
