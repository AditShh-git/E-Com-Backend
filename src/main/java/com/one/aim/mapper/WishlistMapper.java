package com.one.aim.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.one.aim.bo.CartBO;
import com.one.aim.rs.WishlistRs;
import com.one.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WishlistMapper {

	public static WishlistRs mapToWishlistRs(CartBO bo) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToWishlistRs(CartBO) ->");
		}

		try {
			WishlistRs rs = null;

			if (null == bo) {
				log.warn("WishlistBO is NULL");
				return rs;
			}
			rs = new WishlistRs();
			rs.setDocId(String.valueOf(bo.getId()));
			if (Utils.isNotEmpty(bo.getPname())) {
				rs.setPName(bo.getPname());
			}
			if (Utils.isNotEmpty(bo.getDescription())) {
				rs.setDescription(bo.getDescription());
			}
			if (Utils.isNotEmpty(bo.getCategory())) {
				rs.setCategory(bo.getCategory());
			}
			rs.setPrice(bo.getPrice());
			rs.setOffer(bo.getOffer());
			return rs;
		} catch (Exception e) {
			log.error("Exception in mapToWishlistRs(CartBO) - " + e);
			return null;
		}
	}

	public static List<WishlistRs> mapToWishlistRsList(List<CartBO> bos) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToWishlistRsList(CartBO) ->");
		}

		try {
			if (Utils.isEmpty(bos)) {
				log.warn("WishlistBO is NULL");
				return Collections.emptyList();
			}
			List<WishlistRs> rsList = new ArrayList<>();
			for (CartBO bo : bos) {
				WishlistRs rs = mapToWishlistRs(bo);
				if (null != rs) {
					rsList.add(rs);
				}
			}
			return rsList;
		} catch (Exception e) {
			log.error("Exception in mapToWishlistRsList(CartBO) - " + e);
			return Collections.emptyList();
		}
	}

}
