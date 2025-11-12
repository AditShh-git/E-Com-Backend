package com.one.aim.mapper;

import com.one.aim.bo.PaymentBO;
import com.one.aim.rs.PaymentRs;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentMapper {

	public static PaymentRs mapToPaymentRs(PaymentBO bo) {

		if (log.isDebugEnabled()) {
			log.debug("Executing mapToPaymentRs(PaymentBO) ->");
		}

		try {
			PaymentRs rs = null;

			if (null == bo) {
				log.warn("UserBO is NULL");
				return rs;
			}
			rs = new PaymentRs();
			rs.setUserId(String.valueOf(bo.getId()));
			rs.setAmount(String.valueOf(bo.getAmount()));
			rs.setPaymentMethod(bo.getPaymentMethod());
			return rs;
		} catch (Exception e) {
			log.error("Exception in mapToPaymentRs(PaymentBO) - " + e);
			return null;
		}
	}

}
