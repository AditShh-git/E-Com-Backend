package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.one.aim.bo.AttachmentBO;
import com.one.aim.bo.FileBO;
import com.one.aim.rq.AttachmentRq;
import com.one.aim.rs.AttachmentRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AttachmentMapper {

	public static List<AttachmentRs> mapToAttachmentRsList(List<AttachmentBO> bos) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToAttachmentRsList(List<AttachmentBO>) ->");
		}

		try {
			if (Utils.isEmpty(bos)) {
				log.warn("List<AttachmentBO> IS NULL/EMPTY");
				return Collections.<AttachmentRs>emptyList();
			}

			List<AttachmentRs> rsList = new ArrayList<>();
			for (AttachmentBO bo : bos) {
				AttachmentRs rs = mapToAttachmentRs(bo);
				if (null != rs) {
					rsList.add(rs);
				}
			}

			return rsList;
		} catch (Exception e) {
			log.error("Exception in mapToAttachmentRsList(List<AttachmentBO>) - " + e);
			return Collections.<AttachmentRs>emptyList();
		}
	}

	public static AttachmentRs mapToAttachmentRs(AttachmentBO bo) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToAttachmentRs(AttachmentBO) ->");
		}

		try {
			AttachmentRs rs = null;
			if (null == bo) {
				log.warn("AttachmentBO is NULL");
				return rs;
			}

			// Map AttachmentBO to AttachmentRs
			rs = new AttachmentRs();
			BeanUtils.copyProperties(bo, rs, "on");

			rs.setDocId(bo.getDocid());

			return rs;
		} catch (Exception e) {
			log.error("Exception in mapToAttachmentRs(AttachmentBO) - " + e);
			return null;
		}
	}

	public static AttachmentBO prepareAttachmentBO(AttachmentRs rs, String userName) {

		if (log.isDebugEnabled()) {
			log.debug("Executing prepareAttachmentBO(AttachmentRs, UserName) ->");
		}

		try {
			AttachmentBO bo = null;
			if (null == rs) {
				log.warn("AttachmentRs is NULL");
				return bo;
			}

			// Map AttachmentRs to AttachmentBO
			bo = new AttachmentBO();
			// BeanUtils.copyProperties(rs, bo, "by", "on");

			bo.setDocid(rs.getDocId());

			if (Utils.isNotEmpty(rs.getName())) {
				bo.setName(rs.getName());
			}
			if (Utils.isNotEmpty(rs.getDescription())) {
				bo.setDescription(rs.getDescription());
			}
			if (Utils.isNotEmpty(rs.getTitle())) {
				bo.setTitle(rs.getTitle());
			}
			if (Utils.isNotEmpty(rs.getType())) {
				bo.setType(rs.getType());
			}
//            if (Utils.isNotEmpty(rs.getOn())) {
//                bo.setOn(LocalDateTimeUtils.convertDateStringToLdt(rs.getOn()));
//            }
			bo.setBy(Utils.getValidString(userName));
			return bo;
		} catch (Exception e) {
			log.error("Exception in prepareAttachmentBO(AttachmentRs, UserName) - " + e);
			return null;
		}
	}

	public static AttachmentBO prepareAttachmentBO(AttachmentRq rq, FileBO fileBO, String userName) {

		if (log.isDebugEnabled()) {
			log.debug("Executing prepareAttachmentBO(AttachmentRq, FileBO, UserName) ->");
		}

		try {
			AttachmentBO bo = null;
			if (null == rq || fileBO == null) {
				log.warn("AttachmentRs is NULL");
				return bo;
			}

			// Map AttachmentRq to AttachmentBO
			bo = new AttachmentBO();
			bo.setDocid(fileBO.getId());
			bo.setName(fileBO.getName());
			bo.setType(fileBO.getContenttype());
			// bo.setOn(fileBO.getCreatedon());
			if (Utils.isNotEmpty(rq.getDescription())) {
				bo.setDescription(rq.getDescription());
			}
			if (Utils.isNotEmpty(rq.getTitle())) {
				bo.setTitle(rq.getTitle());
			}
			bo.setBy(Utils.getValidString(userName));
			return bo;
		} catch (Exception e) {
			log.error("Exception in prepareAttachmentBO(AttachmentRq, FileBO, UserName) - " + e);
			return null;
		}
	}
}
