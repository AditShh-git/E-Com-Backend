package com.one.utils;

import java.security.MessageDigest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptionUtils {

	public static String makeMD5String(String p_str) {

		if (log.isDebugEnabled()) {
			log.debug("Executing makeMD5String(String) ->");
		}

		try {
			byte[] hash = MessageDigest.getInstance("MD5").digest(p_str.getBytes("UTF-8"));
			StringBuilder l_hex = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				if ((b & 0xFF) < 0x10)
					l_hex.append("0");
				l_hex.append(Integer.toHexString(b & 0xFF));
			}
			return l_hex.toString();
		} catch (Exception e) {
			log.error("Exception in makeMD5String(String) - " + e);
			return null;
		}
	}

	public static boolean checkMD5Password(String normalPassword, String encryptedPassword) {

		if (log.isDebugEnabled()) {
			log.debug("Executing checkMD5Password(normalPassword, encryptedPassword) ->");
		}

		try {
			return makeMD5String(normalPassword).equals(encryptedPassword);
		} catch (Exception e) {
			log.error("Exception in checkMD5Password(normalPassword, encryptedPassword) - " + e);
			return false;
		}
	}

	public static String makeMD5String(byte[] bytes) {

		if (log.isDebugEnabled()) {
			log.debug("Executing makeMD5String(byte[]) ->");
		}

		try {
			byte[] hash = MessageDigest.getInstance("MD5").digest(bytes);
			StringBuilder l_hex = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				if ((b & 0xFF) < 0x10)
					l_hex.append("0");
				l_hex.append(Integer.toHexString(b & 0xFF));
			}
			return l_hex.toString();
		} catch (Exception e) {
			log.error("Exception in makeMD5String(byte[]) - " + e);
			return null;
		}
	}
}
