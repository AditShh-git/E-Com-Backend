package com.one.aim.mapper;

import com.one.aim.bo.AdminBO;
import com.one.aim.rs.AdminRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

//this data for front-end person
@Slf4j
public class AdminMapper {

	public static AdminRs mapToAdminRs(AdminBO bo) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToAdminRs(AdminBO) ->");
		}

		try {
			AdminRs rs = null;

			if (null == bo) {
				log.warn("AdminBO is NULL");
				return rs;
			}
			rs = new AdminRs();
			rs.setDocId(String.valueOf(bo.getId()));
			if (Utils.isNotEmpty(bo.getFullName())) {
				rs.setUserName(bo.getFullName());
			}
			if (Utils.isNotEmpty(bo.getPhoneNo())) {
				rs.setPhoneNo(bo.getPhoneNo());
			}
			if (Utils.isNotEmpty(bo.getEmail())) {
				rs.setEmail(bo.getEmail());
			}
			rs.setRoll(bo.getRole());
			if (bo.getImage() != null) {
				rs.setImage(bo.getImage());
			}
			// rs.setAtts(AttachmentMapper.mapToAttachmentRsList(bo.getAtts()));
			//rs.setImageId(bo.getImageid());
			return rs;
		} catch (Exception e) {
			log.error("Exception in mapToAdminRs(AdminBO) - " + e);
			return null;
		}
	}

}
