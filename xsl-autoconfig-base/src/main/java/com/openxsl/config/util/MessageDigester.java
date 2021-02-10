package com.openxsl.config.util;

import java.security.MessageDigest;

public class MessageDigester {
	
	public static String md5(String text, String salt) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		text = mergePasswordAndSalt(text, salt);
		byte[] bytes = digest.digest(text.getBytes());
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			result.append(Integer.toHexString((0x000000FF & bytes[i]) | 0xFFFFFF00).substring(6));
		}
		return result.toString();
	}
	
	private static String mergePasswordAndSalt(String password, String salt) {
        if (password == null) {
            password = "";
        }

        if ((salt == null) || "".equals(salt)) {
            return password;
        } else {
            return password + '{' + salt.toString() + '}';
        }
    }

}
