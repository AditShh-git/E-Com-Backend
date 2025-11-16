package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.one.aim.bo.DeliveryPersonBO;
import com.one.aim.rs.DeliveryPersonRs;
import com.one.aim.service.FileService;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeliveryPersonMapper {

    public static DeliveryPersonRs mapToDeliveryPersonRs(DeliveryPersonBO bo, FileService fileService) {

        if (log.isDebugEnabled()) {
            log.debug("Executing mapToDeliveryPersonRs(UserBO) ->");
        }

        try {
            if (bo == null) {
                log.warn("DeliveryPersonBO is NULL");
                return null;
            }

            DeliveryPersonRs rs = new DeliveryPersonRs();

            rs.setDocId(bo.getId());
            rs.setName(bo.getFullName());
            rs.setPhone(bo.getPhoneNo());
            rs.setAadhar(bo.getAadharNo());
            rs.setPan(bo.getPan());
            rs.setBikeno(bo.getBikeNo());
            rs.setDriveLience(bo.getDrivingLicence());
            rs.setEmail(bo.getEmail());
            rs.setCity(bo.getCity());

            if (Utils.isNotEmpty(bo.getOrders())) {
                rs.setOrders(
                        OrderMapper.mapToOrderRsList(bo.getOrders(), fileService)
                );
            }

            return rs;

        } catch (Exception e) {
            log.error("Exception in mapToDeliveryPersonRs(UserBO) - " + e);
            return null;
        }
    }


    public static List<DeliveryPersonRs> mapToDeliveryPersonRsList(
            List<DeliveryPersonBO> bos,
            FileService fileService) {

        if (log.isDebugEnabled()) {
            log.debug("Executing mapToDeliveryPersonRsList(DeliveryPersonBO) ->");
        }

        try {
            if (Utils.isEmpty(bos)) {
                log.warn("DeliveryPersonBO list is EMPTY");
                return Collections.emptyList();
            }

            List<DeliveryPersonRs> rsList = new ArrayList<>();

            for (DeliveryPersonBO bo : bos) {
                DeliveryPersonRs rs = mapToDeliveryPersonRs(bo, fileService);
                if (rs != null) {
                    rsList.add(rs);
                }
            }

            return rsList;

        } catch (Exception e) {
            log.error("Exception in mapToDeliveryPersonRsList() - " + e);
            return Collections.emptyList();
        }
    }


}
