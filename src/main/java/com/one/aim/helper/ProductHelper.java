package com.one.aim.helper;

import com.one.aim.rq.ProductRq;
import com.one.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProductHelper {

    public static List<String> validateProduct(ProductRq rq) {
        List<String> errors = new ArrayList<>();

        try {
            if (Utils.isEmpty(rq.getName())) {
                errors.add("EC_REQUIRED_PRODUCT_NAME");
            }
            if (Utils.isEmpty(rq.getDescription())) {
                errors.add("EC_REQUIRED_DESCRIPTION");
            }
            if (rq.getPrice() == null || rq.getPrice() <= 0) {
                errors.add("EC_INVALID_PRICE");
            }
            if (rq.getStock() == null || rq.getStock() < 0) {
                errors.add("EC_INVALID_STOCK");
            }
        } catch (Exception e) {
            log.error("Exception in validateProduct(ProductRq) -> ", e);
            errors.add("EC_INTERNAL_ERROR");
        }

        return errors;
    }
}

