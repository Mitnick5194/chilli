package com.ajie.chilli.utils.common;

public class StringUtil {

	private StringUtil() {
	}

	/**
	 * 字符串是否为空 （null或长度为0）
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return null == str || str.trim().length() == 0;
	}

	/**
	 * 判断两个字符串是否相等，如果两个都为null，返回的是true
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean eq(String str1, String str2) {
		if (str1 == str2) {
			return true;
		}
		if (null == str1)
			return false;
		if (null == str2)
			return false;
		if (str1.equals(str2))
			return true;
		byte[] byte1 = str1.getBytes();
		byte[] byte2 = str2.getBytes();
		if (byte1.length != byte2.length)
			return false;
		for (int i = 0, len = byte1.length; i < len; i++) {
			if (byte1[i] != byte2[i])
				return false;
		}
		return false;
	}

	/**
	 * 去除两头空格是否相等
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean eqTrim(String str1, String str2) {
		if (eq(str1, str2)) {
			return true;
		}
		str1 = str1.trim();
		str2 = str2.trim();
		return eq(str1, str2);
	}

	public static void main(String[] args) {

		String str1 = "abc ";
		String str2 = "abc";
		boolean ret = eqTrim(str1, str2);
		System.out.println(ret);

	}
}
