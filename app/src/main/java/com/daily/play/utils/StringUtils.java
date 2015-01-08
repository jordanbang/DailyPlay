package com.daily.play.utils;

import android.util.Patterns;

public class StringUtils {

	public static boolean isEmptyString(String text) {
		return text == null || text.length() == 0 || text.equalsIgnoreCase("");
	}
	
	public static boolean isNotEmptyString(String text) {
		return !isEmptyString(text);
	}
	
	public static boolean isValidEmail(String text) {	
		return isEmptyString(text) ? false : Patterns.EMAIL_ADDRESS.matcher(text).matches();
	}

	public static boolean isValidPassword(String text) {
		return isEmptyString(text) ? false : text.length() >= 6;
	}
}
