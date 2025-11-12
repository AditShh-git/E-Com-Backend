package com.one.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.one.constants.StringConstants;

//Entire Application Common Util method
public class Utils implements Serializable {

	private static final long serialVersionUID = 1L;

	public static boolean isEmpty(String str) {
		return (((null == str) || (str.trim().isEmpty())) ? true : false);
	}

	public static boolean isNotEmpty(String str) {
		return !(isEmpty(str));
	}

	public static String getValidString(String str) {
		return ((null == str) ? StringConstants.EMPTY : str.trim());
	}

	public static boolean isEmpty(List<?> list) {
		return (((null == list) || (list.isEmpty())) ? true : false);
	}

	public static boolean isNotEmpty(List<?> list) {
		return !(isEmpty(list));
	}

	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Map map) {
		return (((null == map) || (map.isEmpty())) ? true : false);
	}

	@SuppressWarnings("rawtypes")
	public static boolean isNotEmpty(Map map) {
		return !(isEmpty(map));
	}
	
	public static boolean isEmpty(Object[] objs) {
        return (((null == objs) || (0 == objs.length)) ? true : false);
    }

	public static boolean isValidEmail(String email) {
		String strEmail = Utils.getValidString(email);
		if ((Utils.isNotEmpty(strEmail)) && (strEmail
				.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"))) {
			return true;
		}
		return false;
	}
}
