package com.one.aim.rq;

import java.util.Map;

import com.one.vm.core.BaseVM;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRq extends BaseVM {

    private static final long serialVersionUID = 1L;

    private Long addressId;


    private String fullName;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;

    private String paymentMethod;


    private Long amount;
}
