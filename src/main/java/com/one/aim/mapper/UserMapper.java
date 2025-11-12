package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.List;

import com.one.aim.bo.UserBO;
import com.one.aim.rs.UserRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

//this data for front-end person
@Slf4j
public class UserMapper {

	public static UserRs mapToUserRs(UserBO bo) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToUserRs(UserBO) ->");
		}

		try {
			UserRs rs = null;

			if (null == bo) {
				log.warn("UserBO is NULL");
				return rs;
			}
			rs = new UserRs();
			rs.setDocId(bo.getId());
			if (Utils.isNotEmpty(bo.getFullName())) {
				rs.setFullName(bo.getFullName());
			}
			if (Utils.isNotEmpty(bo.getPhoneNo())) {
				rs.setPhoneNo(bo.getPhoneNo());
			}
			if (Utils.isNotEmpty(bo.getEmail())) {
				rs.setEmail(bo.getEmail());
			}
			rs.setRoll(bo.getRole());
			// rs.setAtts(AttachmentMapper.mapToAttachmentRsList(bo.getAtts()));
			if (bo.getImage() != null) {
				rs.setImage(bo.getImage());
			}
			
			//rs.setImageId(bo.getImageid());
			return rs;
		} catch (Exception e) {
			log.error("Exception in mapToUserRs(UserBO) - " + e);
			return null;
		}
	}

	public static List<UserRs> mapToUserRsList(List<UserBO> bos) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToUserRsList(List<UserBO>) ->");
		}

		try {
			List<UserRs> rsList = new ArrayList<>();

			if (Utils.isEmpty(bos)) {
				log.warn("List<UserBO> is NULL");
				return null;
			}
			for (UserBO bo : bos) {
				rsList.add(mapToUserRs(bo));
			}
			return rsList;
		} catch (Exception e) {
			log.error("Exception in mapToUserRsList(List<UserBO>) - " + e);
			return null;
		}
	}

}
