package com.one.aim.service.impl;

import java.util.List;
import java.util.Optional;

import com.one.aim.repo.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.one.aim.bo.AddressBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.helper.AddressHelper;
import com.one.aim.repo.AddressRepo;
import com.one.aim.rq.AddressRq;
import com.one.aim.rs.AddressDataRs;
import com.one.aim.rs.data.UserDataRs;
import com.one.aim.service.AddressService;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private OrderRepo orderRepo;

    // =========================================================
    // SAVE NEW ADDRESS
    // =========================================================
    @Override
    public BaseRs saveAddress(AddressRq rq) throws Exception {

        List<String> errors = AddressHelper.validateAddress(rq);
        if (Utils.isNotEmpty(errors)) {
            return ResponseUtils.failure(ErrorCodes.EC_INVALID_INPUT, errors);
        }

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        boolean hasAddress = !addressRepo.findByUserid(userId).isEmpty();

        AddressBO address = new AddressBO();
        address.setFullName(rq.getFullName());
        address.setStreet(rq.getStreet());
        address.setCity(rq.getCity());
        address.setState(rq.getState());
        address.setZip(rq.getZip());
        address.setCountry(rq.getCountry());
        address.setPhone(rq.getPhone());
        address.setUserid(userId);

        // first address = default
        address.setIsDefault(!hasAddress);

        addressRepo.save(address);

        return ResponseUtils.success(new AddressDataRs("Address saved successfully"));
    }

    // =========================================================
    // GET ADDRESS BY ID
    // =========================================================
    @Override
    public AddressBO getAddressById(Long id) throws Exception {

        return addressRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found: " + id));
    }

    // =========================================================
    // GET ALL ADDRESSES OF USER
    // =========================================================
    @Override
    public BaseRs getAllAddressesOfLoggedUser() throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        List<AddressBO> list = addressRepo.findByUserid(userId);

        List<AddressDataRs> dataList = list.stream().map(a -> {
            AddressDataRs d = new AddressDataRs("Address retrieved");
            copy(d, a);
            return d;
        }).toList();

        return ResponseUtils.success(dataList);
    }

    // Utility mapper
    private void copy(AddressDataRs d, AddressBO a) {
        d.setId(a.getId());
        d.setFullName(a.getFullName());
        d.setStreet(a.getStreet());
        d.setCity(a.getCity());
        d.setState(a.getState());
        d.setZip(a.getZip());
        d.setCountry(a.getCountry());
        d.setPhone(a.getPhone());
        d.setUserid(a.getUserid());
    }

    // =========================================================
    // SET DEFAULT ADDRESS
    // =========================================================
    @Override
    @Transactional
    public BaseRs setDefaultAddress(Long addressId) throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        List<AddressBO> list = addressRepo.findByUserid(userId);
        list.forEach(a -> a.setIsDefault(false));

        AddressBO selected = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // ensure user owns the address
        if (!selected.getUserid().equals(userId)) {
            throw new RuntimeException("You cannot set another user's address as default");
        }

        selected.setIsDefault(true);

        addressRepo.saveAll(list);

        return ResponseUtils.success("Default address updated");
    }

    // =========================================================
    // UPDATE ADDRESS
    // =========================================================
    @Override
    @Transactional
    public BaseRs updateAddress(Long addressId, AddressRq rq) throws Exception {

        AddressBO address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        Long userId = AuthUtils.findLoggedInUser().getDocId();
        if (!address.getUserid().equals(userId)) {
            throw new RuntimeException("Unauthorized: Not your address");
        }

        // update only provided fields
        if (Utils.isNotEmpty(rq.getFullName())) address.setFullName(rq.getFullName());
        if (Utils.isNotEmpty(rq.getStreet())) address.setStreet(rq.getStreet());
        if (Utils.isNotEmpty(rq.getCity())) address.setCity(rq.getCity());
        if (Utils.isNotEmpty(rq.getState())) address.setState(rq.getState());
        if (Utils.isNotEmpty(rq.getZip())) address.setZip(rq.getZip());
        if (Utils.isNotEmpty(rq.getCountry())) address.setCountry(rq.getCountry());
        if (Utils.isNotEmpty(rq.getPhone())) address.setPhone(rq.getPhone());

        addressRepo.save(address);

        return ResponseUtils.success("Address updated successfully");
    }

    // =========================================================
    // DELETE ADDRESS
    // =========================================================
    @Override
    @Transactional
    public BaseRs deleteAddress(Long addressId) throws Exception {

        AddressBO address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Check if address is used in any order
        boolean isUsedInOrder = orderRepo.existsByShippingAddress(address);

        if (isUsedInOrder) {
            //  Soft delete
            address.setIsDefault(false);
            addressRepo.save(address);
            return ResponseUtils.success("Address removed from your list");
        }

        // Hard delete (if never used)
        addressRepo.delete(address);

        return ResponseUtils.success("Address deleted successfully");
    }


    @Override
    public BaseRs getAddressOfLoggedInUser() throws Exception {

        Long userId = AuthUtils.findLoggedInUser().getDocId();

        // Try to fetch default address
        Optional<AddressBO> optional =
                addressRepo.findFirstByUseridAndIsDefault(userId, true);

        // If not found → AUTO SET first address as default
        if (optional.isEmpty()) {

            List<AddressBO> list = addressRepo.findByUserid(userId);

            if (list.isEmpty()) {
                throw new RuntimeException("No address found for logged-in user");
            }

            AddressBO first = list.get(0);
            first.setIsDefault(true);
            addressRepo.save(first);

            optional = Optional.of(first);
        }

        AddressBO address = optional.get();

        AddressDataRs data = new AddressDataRs("Address retrieved successfully");
        data.setId(address.getId());
        data.setFullName(address.getFullName());
        data.setStreet(address.getStreet());
        data.setCity(address.getCity());
        data.setState(address.getState());
        data.setZip(address.getZip());
        data.setCountry(address.getCountry());
        data.setPhone(address.getPhone());
        data.setUserid(address.getUserid());

        return ResponseUtils.success(data);
    }

}
