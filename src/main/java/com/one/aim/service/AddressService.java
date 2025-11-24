package com.one.aim.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.one.aim.bo.AddressBO;
import com.one.aim.rq.AddressRq;
import com.one.vm.core.BaseRs;

public interface AddressService {

    // Save new address
    BaseRs saveAddress(AddressRq rq) throws Exception;

    // Get address by ID
    AddressBO getAddressById(Long id) throws Exception;

    // Get DEFAULT address of logged-in user
    BaseRs getAddressOfLoggedInUser() throws Exception;

    //  Get all addresses of logged-in user
    BaseRs getAllAddressesOfLoggedUser() throws Exception;

    //  Set an address as default
    BaseRs setDefaultAddress(Long addressId) throws Exception;

    //  Update an address (optional, but real e-commerce)
    BaseRs updateAddress(Long addressId, AddressRq rq) throws Exception;

    //  Delete an address
    BaseRs deleteAddress(Long addressId) throws Exception;
}
