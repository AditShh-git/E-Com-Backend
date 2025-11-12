package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.List;

import com.one.aim.bo.VendorBO;
import com.one.aim.rs.VendorRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VendorMapper {

	public static VendorRs mapToVendorRs(VendorBO bo) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToAdminRs(AdminBO) ->");
		}

		try {
			VendorRs rs = null;

			if (null == bo) {
				log.warn("AdminBO is NULL");
				return rs;
			}
			rs = new VendorRs();
			rs.setDocId(String.valueOf(bo.getId()));
			if (Utils.isNotEmpty(bo.getFullName())) {
				rs.setUserName(bo.getFullName());
			}
			if (Utils.isNotEmpty(bo.getPhoneNo())) {
				rs.setPhoneNo(bo.getPhoneNo());
			}
			if (Utils.isNotEmpty(bo.getPancard())) {
				rs.setPancard(bo.getPancard());
			}
			if (Utils.isNotEmpty(bo.getGst())) {
				rs.setGst(bo.getGst());
			}
			if (Utils.isNotEmpty(bo.getAdhaar())) {
				rs.setAdhaar(bo.getAdhaar());
			}
			if (Utils.isNotEmpty(bo.getEmail())) {
				rs.setEmail(bo.getEmail());
			}
			rs.setVarified(bo.isVarified());
			rs.setRoll(bo.getRoll());
			if (bo.getImage() != null) {
				rs.setImage(bo.getImage());
			}
			// rs.setAtts(AttachmentMapper.mapToAttachmentRsList(bo.getAtts()));
			return rs;
		} catch (Exception e) {
			log.error("Exception in mapToAdminRs(AdminBO) - " + e);
			return null;
		}
	}

	public static List<VendorRs> mapToVendorRsList(List<VendorBO> bos) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToVendorRsList(AdminBO) ->");
		}

		try {
			List<VendorRs> rsList = new ArrayList<>();

			if (Utils.isEmpty(bos)) {
				log.warn("VendorBOs is NULL");
				return rsList;
			}
			for (VendorBO bo : bos) {
				rsList.add(mapToVendorRs(bo));
			}
			return rsList;
		} catch (Exception e) {
			log.error("Exception in mapToVendorRsList(AdminBO) - " + e);
			return null;
		}
	}

}
