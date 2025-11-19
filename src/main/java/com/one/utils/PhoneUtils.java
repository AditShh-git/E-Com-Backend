package com.one.utils;

public class PhoneUtils {

    public static String normalize(String phone) {
        if (phone == null) return null;

        phone = phone.replaceAll("\\D", "");

        if (phone.length() == 12 && phone.startsWith("91")) {
            phone = phone.substring(2);
        }

        return phone;
    }
}

