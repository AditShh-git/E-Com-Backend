package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.one.aim.bo.DeliveryPersonBO;
import com.one.aim.rs.DeliveryPersonRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeliveryPersonMapper {

	public static DeliveryPersonRs mapToDeliveryPersonRs(DeliveryPersonBO bo) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToDeliveryPersonRs(UserBO) ->");
		}

		try {
			DeliveryPersonRs rs = null;

			if (null == bo) {
				log.warn("UserBO is NULL");
				return rs;
			}
			rs = new DeliveryPersonRs();
			rs.setDocId(bo.getId());
			if (Utils.isNotEmpty(bo.getFullName())) {
				rs.setName(bo.getFullName());
			}
			if (Utils.isNotEmpty(bo.getPhoneNo())) {
				rs.setPhone(bo.getPhoneNo());
			}
			if (Utils.isNotEmpty(bo.getAadharNo())) {
				rs.setAadhar(bo.getAadharNo());
			}
			if (Utils.isNotEmpty(bo.getPan())) {
				rs.setPan(bo.getPan());
			}
			if (Utils.isNotEmpty(bo.getBikeNo())) {
				rs.setBikeno(bo.getBikeNo());
			}
			if (Utils.isNotEmpty(bo.getDrivingLicence())) {
				rs.setDriveLience(bo.getDrivingLicence());
			}
			if (Utils.isNotEmpty(bo.getEmail())) {
				rs.setEmail(bo.getEmail());
			}
			if (Utils.isNotEmpty(bo.getCity())) {
				rs.setCity(bo.getCity());
			}
			if (Utils.isNotEmpty(bo.getOrders())) {
				rs.setOrders(OrderMapper.mapToOrderRsList(bo.getOrders()));
			}
			// rs.setAtts(AttachmentMapper.mapToAttachmentRsList(bo.getAtts()));
			return rs;
		} catch (Exception e) {
			log.error("Exception in mapToDeliveryPersonRs(UserBO) - " + e);
			return null;
		}
	}

	public static List<DeliveryPersonRs> mapToDeliveryPersonRsList(List<DeliveryPersonBO> bos) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToOrderRsList(OrderBO) ->");
		}

		try {
			if (Utils.isEmpty(bos)) {
				log.warn("OrderBO is NULL");
				return Collections.emptyList();
			}

			List<DeliveryPersonRs> rsList = new ArrayList<>();
			for (DeliveryPersonBO bo : bos) {
				DeliveryPersonRs rs = mapToDeliveryPersonRs(bo);
				if (null != rs) {
					rsList.add(rs);
				}
			}
			return rsList;
		} catch (Exception e) {
			log.error("Exception in mapToOrderRsList(OrderBO) - " + e);
			return Collections.emptyList();
		}
	}

}
