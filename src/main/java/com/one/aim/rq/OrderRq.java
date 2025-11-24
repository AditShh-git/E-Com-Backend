package com.one.aim.rq;

import com.one.vm.core.BaseVM;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRq extends BaseVM {

    private static final long serialVersionUID = 1L;

    // If user selects an existing saved address
    private Long addressId;

    // If user enters a new address manually during checkout
    private String fullName;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;

    // COD / ONLINE
    private String paymentMethod;

    // Payment amount
    private Long amount;
}
