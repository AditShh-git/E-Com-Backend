package com.one.aim.service.impl;

import java.util.List;
import java.util.Optional;

import com.one.aim.repo.AdminRepo;
import com.one.aim.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.one.aim.bo.DeliveryPersonBO;
import com.one.aim.bo.UserBO;
import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.aim.mapper.DeliveryPersonMapper;
import com.one.aim.repo.DeliveryPersonRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.rq.DeliveryPersonRq;
import com.one.aim.rs.DeliveryPersonRs;
import com.one.aim.rs.data.DeliveryPersonDataRs;
import com.one.aim.rs.data.DeliveryPersonDataRsList;
import com.one.aim.service.DeliveryPersonService;
import com.one.constants.StringConstants;
import com.one.utils.AuthUtils;
import com.one.utils.Utils;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeliveryPersonServiceImpl implements DeliveryPersonService {

	@Autowired
	DeliveryPersonRepo deliveryPersonRepo;

	@Autowired
	UserRepo userRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

    @Autowired
    private FileService fileService;

    @Autowired
    private AdminRepo adminRepo;

    @Override
    public BaseRs saveDeliveryPerson(DeliveryPersonRq rq) {

        if (log.isDebugEnabled()) {
            log.debug("Executing saveDeliveryPerson(CompanyRq) ->");
        }

        String docId = Utils.getValidString(rq.getDocId());
        String message = StringConstants.EMPTY;
        DeliveryPersonBO userBO;

        // UPDATE
        if (Utils.isNotEmpty(docId)) {
            long id = Long.parseLong(docId);
            userBO = deliveryPersonRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException(ErrorCodes.EC_USER_NOT_FOUND));
        }
        // CREATE
        else {
            userBO = new DeliveryPersonBO();
            message = MessageCodes.MC_SAVED_SUCCESSFUL;
        }

        String email = Utils.getValidString(rq.getEmail());
        if (!email.equals(userBO.getEmail())) userBO.setEmail(email);

        String userName = Utils.getValidString(rq.getName());
        if (!userName.equals(userBO.getFullName())) userBO.setFullName(userName);

        String phoneNo = Utils.getValidString(rq.getPhoneno());
        if (!phoneNo.equals(userBO.getPhoneNo())) userBO.setPhoneNo(phoneNo);

        String aadhar = Utils.getValidString(rq.getAadhar());
        if (!aadhar.equals(userBO.getAadharNo())) userBO.setAadharNo(aadhar);

        String pan = Utils.getValidString(rq.getPan());
        if (!pan.equals(userBO.getPan())) userBO.setPan(pan);

        String bikeNo = Utils.getValidString(rq.getBikeno());
        if (!bikeNo.equals(userBO.getBikeNo())) userBO.setBikeNo(bikeNo);

        String driveLience = Utils.getValidString(rq.getDriveLience());
        if (!driveLience.equals(userBO.getDrivingLicence())) userBO.setDrivingLicence(driveLience);

        String city = Utils.getValidString(rq.getCity());
        if (!city.equals(userBO.getCity())) userBO.setCity(city);

        String rawPassword = Utils.getValidString(rq.getPassword());
        String existingEncodedPassword = userBO.getPassword();

        // Update password only if changed
        if (!passwordEncoder.matches(rawPassword, existingEncodedPassword)) {
            userBO.setPassword(passwordEncoder.encode(rawPassword));
        }

        deliveryPersonRepo.save(userBO);

        // ******* UPDATED LINE ↓↓↓ ********
        DeliveryPersonRs deliveryPersonRs =
                DeliveryPersonMapper.mapToDeliveryPersonRs(userBO, fileService);

        return ResponseUtils.success(new DeliveryPersonDataRs(message, deliveryPersonRs));
    }


    @Override
    public BaseRs retrieveDeliveryPersons() {

        if (log.isDebugEnabled()) {
            log.debug("Executing retrieveDeliveryPersons() ->");
        }

        try {
            // Only Admin can view all delivery persons → block USER
            UserBO userBO = userRepo.findByFullNameAndId(
                    AuthUtils.findLoggedInUser().getFullName(),
                    AuthUtils.findLoggedInUser().getDocId());

            if (userBO != null) {
                log.error(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
            }

            List<DeliveryPersonBO> deliveryPersonBOs = deliveryPersonRepo.findAll();
            String message = MessageCodes.MC_RETRIEVED_SUCCESSFUL;

            if (Utils.isEmpty(deliveryPersonBOs)) {
                message = MessageCodes.MC_NO_RECORDS_FOUND;
            }

            // ✅ Updated to pass fileService
            List<DeliveryPersonRs> rsList =
                    DeliveryPersonMapper.mapToDeliveryPersonRsList(deliveryPersonBOs, fileService);

            return ResponseUtils.success(new DeliveryPersonDataRsList(message, rsList));

        } catch (Exception e) {
            log.error("Exception in retrieveDeliveryPersons() -> " + e);
            return null;
        }
    }


    @Override
    public BaseRs retrieveDeliveryPersonById(String id) {

        if (log.isDebugEnabled()) {
            log.debug("Executing retrieveDeliveryPersonById(id) ->");
        }

        try {
            // Allow only ADMIN to access
            Long loggedId = AuthUtils.findLoggedInUser().getDocId();
            String loggedName = AuthUtils.findLoggedInUser().getFullName();

            boolean isAdmin = adminRepo.findByIdAndFullName(loggedId, loggedName) != null;

            if (!isAdmin) {
                return ResponseUtils.failure(ErrorCodes.EC_UNAUTHORIZED_ACCESS);
            }

            Optional<DeliveryPersonBO> opt = deliveryPersonRepo.findById(Long.parseLong(id));

            if (opt.isEmpty()) {
                return ResponseUtils.failure(MessageCodes.MC_NO_RECORDS_FOUND);
            }

            DeliveryPersonRs rs =
                    DeliveryPersonMapper.mapToDeliveryPersonRs(opt.get(), fileService);

            return ResponseUtils.success(
                    new DeliveryPersonDataRs(MessageCodes.MC_RETRIEVED_SUCCESSFUL, rs)
            );
        }
        catch (Exception e) {
            log.error("Exception in retrieveDeliveryPersonById(id) -> " + e);
            return ResponseUtils.failure(ErrorCodes.EC_INTERNAL_ERROR);
        }
    }


}
