package com.one.vm.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.one.aim.constants.ErrorCodes;
import com.one.aim.constants.MessageCodes;
import com.one.constants.StringConstants;
import com.one.utils.ErrorBundles;
import com.one.utils.MessageBundles;
import com.one.utils.Utils;
import com.one.vm.common.ErrorRs;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseErrorRs;
import com.one.vm.core.BaseRs;

import lombok.extern.slf4j.Slf4j;

//this class for return success or failure status
@Slf4j
public class ResponseUtils {

	public static BaseRs success(BaseDataRs dataRs) {

		if (log.isDebugEnabled()) {
			log.debug("Executing success(BaseDataRs) ->");
		}

		BaseRs rs = new BaseRs();
		rs.setStatus(StringConstants.SUCCESS);
		if (null != dataRs) {
			rs.setData(dataRs);
		}

		return rs;
	}

	public static BaseRs success(String code) {

		if (log.isDebugEnabled()) {
			log.debug("Executing success(Code) ->");
		}

		String message = MessageBundles.getMessage(code);
		if (Utils.isEmpty(message)) {
			message = code;
		}

		BaseRs rs = new BaseRs();
		rs.setStatus(StringConstants.SUCCESS);
		rs.setData(new BaseDataRs(message));

		return rs;
	}

    public static BaseRs success(Object data) {
        if (log.isDebugEnabled()) {
            log.debug("Executing success(Object data) ->");
        }

        BaseRs rs = new BaseRs();
        rs.setStatus(StringConstants.SUCCESS);

        // Wrap the given data object into BaseDataRs, reusing your structure
        BaseDataRs baseDataRs = new BaseDataRs(MessageCodes.MC_SUCCESS);
        baseDataRs.setData(data);

        rs.setData(baseDataRs);
        return rs;
    }

	public static BaseRs failure(String errorCode) {

		if (log.isDebugEnabled()) {
			log.debug("Executing failure(errorCode) ->");
		}

		BaseRs rs = new BaseRs();
		rs.setStatus(StringConstants.FAILURE);
		if (Utils.isNotEmpty(errorCode)) {
			BaseErrorRs baseErrorVM = new BaseErrorRs();
			baseErrorVM.setCode(errorCode);
			baseErrorVM.setMessage(ErrorBundles.getProperty(errorCode));
			baseErrorVM.setErrors(populateErrorRsList(errorCode));
			rs.setError(baseErrorVM);
		}

		return rs;
	}

	public static BaseRs failure(Exception e) {

		if (log.isDebugEnabled()) {
			log.debug("Executing failure(Exception) ->");
		}

		BaseRs rs = new BaseRs();
		rs.setStatus(StringConstants.FAILURE);
		if (null != e) {
			BaseErrorRs baseErrorRs = new BaseErrorRs();
			baseErrorRs.setCode(ErrorCodes.EC_UNKNOWN_ERROR);
			baseErrorRs.setMessage(ErrorBundles.getProperty(ErrorCodes.EC_UNKNOWN_ERROR));
			baseErrorRs.setErrors(populateErrorRsList(e.getMessage()));
			rs.setError(baseErrorRs);
		}

		return rs;
	}

    public static BaseRs failure(String errorCode, String message) {

        if (log.isDebugEnabled()) {
            log.debug("Executing failure(errorCode, message) ->");
        }

        BaseRs rs = new BaseRs();
        rs.setStatus(StringConstants.FAILURE);

        if (Utils.isNotEmpty(errorCode)) {
            BaseErrorRs baseErrorRs = new BaseErrorRs();
            baseErrorRs.setCode(errorCode);
            // If a custom message is provided, use it; otherwise fallback to bundle
            baseErrorRs.setMessage(Utils.isNotEmpty(message)
                    ? message
                    : ErrorBundles.getProperty(errorCode));

            // Wrap message into a single-element list for consistency
            baseErrorRs.setErrors(populateErrorRsList(errorCode));
            rs.setError(baseErrorRs);
        }

        return rs;
    }

	public static BaseRs failure(String errorCode, List<String> errorCodes) {

		if (log.isDebugEnabled()) {
			log.debug("Executing failure(errorCode, errorCodes) ->");
		}

		BaseRs rs = new BaseRs();
		rs.setStatus(StringConstants.FAILURE);

		if (Utils.isNotEmpty(errorCode)) {
			if (log.isDebugEnabled()) {
				log.error(errorCode);
			}

			BaseErrorRs baseErrorRs = new BaseErrorRs();
			baseErrorRs.setCode(errorCode);
			baseErrorRs.setMessage(ErrorBundles.getProperty(errorCode));
			baseErrorRs.setErrors(populateErrorRsList(errorCodes));
			rs.setError(baseErrorRs);
		}

		return rs;
	}

	public static BaseRs failure(String errorCode, List<String> errorCodes, int limit) {

		if (log.isDebugEnabled()) {
			log.debug("Executing failure(errorCode, errorCodes, Limit) ->");
		}

		BaseRs rs = new BaseRs();
		rs.setStatus(StringConstants.FAILURE);

		if (Utils.isNotEmpty(errorCode)) {

			if (log.isDebugEnabled()) {
				log.error(errorCode);
			}

			BaseErrorRs baseErrorRs = new BaseErrorRs();
			baseErrorRs.setCode(errorCode);
			baseErrorRs.setMessage(ErrorBundles.getProperty(errorCode));
			if (Utils.isNotEmpty(errorCodes)) {
				if ((limit > 0) && (errorCodes.size() >= limit)) {
					baseErrorRs.setErrors(populateErrorRsList(errorCodes.subList(0, (limit - 1))));
				} else {
					baseErrorRs.setErrors(populateErrorRsList(errorCodes));
				}
			}
			rs.setError(baseErrorRs);
		}

		return rs;
	}

	public static BaseRs failure(String errorCode, Map<Integer, String> errorCodes) {

		if (log.isDebugEnabled()) {
			log.debug("Executing failure(ErrorCode, ErrorCodes) ->");
		}

		BaseRs rsVM = new BaseRs();
		rsVM.setStatus(StringConstants.FAILURE);

		if (Utils.isNotEmpty(errorCode)) {
			if (log.isDebugEnabled()) {
				log.error(errorCode);
			}

			BaseErrorRs baseErrorRs = new BaseErrorRs();
			baseErrorRs.setCode(errorCode);
			baseErrorRs.setMessage(ErrorBundles.getProperty(errorCode));
			baseErrorRs.setErrors(populateErrorVMList(errorCodes));
			rsVM.setError(baseErrorRs);
		}

		return rsVM;
	}

	private static List<ErrorRs> populateErrorVMList(Map<Integer, String> errorCodes) {

		if (log.isDebugEnabled()) {
			log.debug("Executing populateErrorVMList(Map<String, String> errorCodes) ->");
		}
		List<ErrorRs> errorVMs = new ArrayList<>();

		if (Utils.isEmpty(errorCodes)) {
			return errorVMs;
		}
		for (Map.Entry<Integer, String> entry : errorCodes.entrySet()) {
			ErrorRs errorVM = null;
			if (Utils.isNotEmpty(entry.getValue())) {
				errorVM = new ErrorRs();
				errorVM.setCode(String.valueOf(entry.getKey()));
				errorVM.setMessage(getMessageStr(entry.getValue()));
			}
			if (errorVM != null) {
				errorVMs.add(errorVM);
			}
		}
		return errorVMs;
	}

	private static String getMessageStr(String str) {

		if (log.isDebugEnabled()) {
			log.debug("Executing getMessageStr(Str) ->");
		}

		String msg = StringConstants.EMPTY;
		if (Utils.isEmpty(str)) {
			return msg;
		}
		String[] strArr = Utils.getValidString(str).split(StringConstants.COMMA);
		for (String message : strArr) {
			if (Utils.isEmpty(msg)) {
				msg += ErrorBundles.getProperty(message) + StringConstants.COMMA;
			} else {
				msg += StringConstants.SPACE + ErrorBundles.getProperty(message) + StringConstants.COMMA;
			}
		}
		if (Utils.isNotEmpty(msg) && msg.endsWith(StringConstants.COMMA)) {
			msg = msg.substring(0, msg.length() - 1);
		}

		return msg;
	}

	private static List<ErrorRs> populateErrorRsList(List<String> errorCodes) {

		if (log.isDebugEnabled()) {
			log.debug("Executing populateErrorRsList(List<String> errorCodes) ->");
		}

		List<ErrorRs> errorRsList = new ArrayList<>();

		if (Utils.isEmpty(errorCodes)) {
			return errorRsList;
		}

		for (String errorCode : errorCodes) {
			if (errorCode.startsWith("EC")) {
				ErrorRs errorRs = populateErrorRs(errorCode);
				if (null != errorRs) {
					errorRsList.add(errorRs);
				}
			} else {
				List<ErrorRs> errorList = populateReportInputErrorRsList(errorCode);
				if (Utils.isNotEmpty(errorList)) {
					errorRsList.addAll(errorList);
				}
			}

		}
		return errorRsList;
	}

	private static List<ErrorRs> populateErrorRsList(String errorCode) {

		if (log.isDebugEnabled()) {
			log.debug("Executing populateErrorRsList(ErrorCodes) ->");
		}

		List<ErrorRs> errorRsList = new ArrayList<>();

		if (Utils.isEmpty(errorCode)) {
			return errorRsList;
		}

		ErrorRs errorRs = populateErrorRs(errorCode);
		if (null != errorRs) {
			errorRsList.add(errorRs);
		}
		return errorRsList;
	}

	private static ErrorRs populateErrorRs(String errorCode) {

		if (log.isDebugEnabled()) {
			log.debug("Executing populateErrorRs(String errorCode) ->");
		}

		ErrorRs errorRs = null;

		if (Utils.isEmpty(errorCode)) {
			return errorRs;
		}

		if (log.isDebugEnabled()) {
			log.error(errorCode);
		}

		errorRs = new ErrorRs();
		errorRs.setCode(errorCode);
		errorRs.setMessage(ErrorBundles.getProperty(errorCode));
		return errorRs;
	}

	private static List<ErrorRs> populateReportInputErrorRsList(String errorCodes) {

		if (log.isDebugEnabled()) {
			log.debug("Executing populateReportInputErrorRsList(ErrorCodes) ->");
		}
		List<ErrorRs> errorRsList = new ArrayList<>();

		if (Utils.isNotEmpty(errorCodes)) {
			List<String> errorCodeStrList = Arrays.asList(errorCodes.split(StringConstants.COMMA));
			for (String errorCodeStr : errorCodeStrList) {
				ErrorRs errorRs = populateReportInputErrorRs(errorCodeStr);
				if (errorRs != null) {
					errorRsList.add(errorRs);
				}
			}
		}
		return errorRsList;
	}

	private static ErrorRs populateReportInputErrorRs(String errorCode) {

		if (log.isDebugEnabled()) {
			log.debug("Executing populateReportInputErrorRs(ErrorCode) ->");
		}
		ErrorRs errorRs = null;
		if (Utils.isNotEmpty(errorCode)) {
			List<String> errorCodeStr = Arrays.asList(errorCode.split(StringConstants.COLON));
			if (errorCodeStr.size() == 2) {
				String key = errorCodeStr.get(0);
				String code = errorCodeStr.get(1);

				errorRs = new ErrorRs();
				errorRs.setCode(code);
				errorRs.setMessage(key + StringConstants.SPACE + StringConstants.HYPHEN + StringConstants.SPACE
						+ ErrorBundles.getProperty(code));
			}
		}
		return errorRs;
	}
}
