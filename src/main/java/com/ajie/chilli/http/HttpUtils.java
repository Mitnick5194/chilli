package com.ajie.chilli.http;

/**
 * http辅助工具
 * 
 * @author ajie
 *
 */
public class HttpUtils {

	/**
	 * 断言http协议
	 * 
	 * @param url
	 *            链接
	 */
	public static void assertHttpProtocol(String url) {
		if (null == url) {
			throw new IllegalArgumentException("无效链接：" + url);
		}
		if (!url.startsWith("http") && !url.startsWith("https")) {
			throw new IllegalArgumentException("无效协议：" + url);
		}
	}

}
