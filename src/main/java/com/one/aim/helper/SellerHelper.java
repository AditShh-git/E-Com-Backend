package com.one.aim.helper;


import com.one.aim.constants.ErrorCodes;
import com.one.aim.rq.SellerRq;
import com.one.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SellerHelper {

    /**
     * Default (signup) validation.
     */
    public static List<String> validateSeller(SellerRq rq) {
        return validateSeller(rq, false);
    }

    /**
     * Context-aware validation for SellerRq.
     *
     * @param rq       request object
     * @param isUpdate true when validating for profile update (relaxed required fields)
     * @return list of error codes (empty if valid)
     */
    public static List<String> validateSeller(SellerRq rq, boolean isUpdate) {
        if (log.isDebugEnabled()) {
            log.debug("Executing validateSeller(SellerRq, isUpdate={}) ->", isUpdate);
        }

        List<String> errors = new ArrayList<>();
        try {
            // null-check
            if (rq == null) {
                log.error("SellerRq is null");
                errors.add(ErrorCodes.EC_INVALID_INPUT);
                return errors;
            }

            // ===========================================================
            // FULL NAME
            // ===========================================================
            if (!isUpdate) { // signup requires full name
                if (Utils.isEmpty(rq.getFullName())) {
                    log.error(ErrorCodes.EC_REQUIRED_FULLNAME);
                    errors.add(ErrorCodes.EC_REQUIRED_FULLNAME);
                } else if (rq.getFullName().length() > 100) {
                    log.error(ErrorCodes.EC_INVALID_INPUT);
                    errors.add(ErrorCodes.EC_INVALID_INPUT);
                }
            } else { // update: if provided must be valid
                if (Utils.isNotEmpty(rq.getFullName()) && rq.getFullName().length() > 100) {
                    log.error("Full name too long");
                    errors.add(ErrorCodes.EC_INVALID_INPUT);
                }
            }

            // ===========================================================
            // EMAIL
            // ===========================================================
            String email = Utils.getValidString(rq.getEmail()).toLowerCase();
            if (!isUpdate) { // signup requires email
                if (Utils.isEmpty(email)) {
                    log.error(ErrorCodes.EC_REQUIRED_EMAIL);
                    errors.add(ErrorCodes.EC_REQUIRED_EMAIL);
                } else if (!Utils.isValidEmail(email)) {
                    log.error(ErrorCodes.EC_INVALID_EMAIL);
                    errors.add(ErrorCodes.EC_INVALID_EMAIL);
                } else if (email.length() > 100) {
                    log.error(ErrorCodes.EC_INVALID_EMAIL);
                    errors.add(ErrorCodes.EC_INVALID_EMAIL);
                }
            } else { // update: email optional but if present must be valid
                if (Utils.isNotEmpty(email)) {
                    if (!Utils.isValidEmail(email) || email.length() > 100) {
                        log.error(ErrorCodes.EC_INVALID_EMAIL);
                        errors.add(ErrorCodes.EC_INVALID_EMAIL);
                    }
                }
            }

            // ===========================================================
            // PASSWORD
            // ===========================================================
            String password = Utils.getValidString(rq.getPassword());
            if (!isUpdate) { // signup requires password
                if (Utils.isEmpty(password)) {
                    log.error(ErrorCodes.EC_REQUIRED_PASSWORD);
                    errors.add(ErrorCodes.EC_REQUIRED_PASSWORD);
                } else if (password.length() < 6) {
                    log.error(ErrorCodes.EC_INVALID_PASSWORD);
                    errors.add(ErrorCodes.EC_INVALID_PASSWORD);
                }
            } else { // update: password optional (if present must meet criteria)
                if (Utils.isNotEmpty(password) && password.length() < 6) {
                    log.error(ErrorCodes.EC_INVALID_PASSWORD);
                    errors.add(ErrorCodes.EC_INVALID_PASSWORD);
                }
            }

            // ===========================================================
            // PHONE NUMBER (optional)
            // ===========================================================
            if (Utils.isNotEmpty(rq.getPhoneNo())) {
                if (!rq.getPhoneNo().matches("^[6-9]\\d{9}$")) {
                    log.error(ErrorCodes.EC_INVALID_PHONE);
                    errors.add(ErrorCodes.EC_INVALID_PHONE);
                }
            }

            // ===========================================================
            // PAN (optional)
            // ===========================================================
            if (Utils.isNotEmpty(rq.getPanCard())) {
                if (!rq.getPanCard().matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")) {
                    log.error(ErrorCodes.EC_INVALID_PAN);
                    errors.add(ErrorCodes.EC_INVALID_PAN);
                }
            }

            // ===========================================================
            // GST (optional)
            // ===========================================================
            if (Utils.isNotEmpty(rq.getGst())) {
                // basic GSTIN pattern check (can be tuned per business rules)
                if (!rq.getGst().matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) {
                    log.error(ErrorCodes.EC_INVALID_GST);
                    errors.add(ErrorCodes.EC_INVALID_GST);
                }
            }

            // ===========================================================
            // AADHAAR (optional)
            // ===========================================================
            if (Utils.isNotEmpty(rq.getAdhaar())) {
                if (!rq.getAdhaar().matches("^[0-9]{12}$")) {
                    log.error(ErrorCodes.EC_INVALID_AADHAAR);
                    errors.add(ErrorCodes.EC_INVALID_AADHAAR);
                }
            }

            // ===========================================================
            // IMAGE (optional) - basic check for file size/name handled by controller/service
            // ===========================================================
            // Do not validate MultipartFile contents here — fileService will handle validations.
        } catch (Exception e) {
            log.error("Exception in validateSeller(SellerRq, isUpdate) - " + e.getMessage(), e);
            errors.add(ErrorCodes.EC_INVALID_INPUT);
        }

        return errors;
    }
}