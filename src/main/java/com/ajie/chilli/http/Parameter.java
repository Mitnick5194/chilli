package com.ajie.chilli.http;

/**
 * 简单的请求参数
 * 
 * @author niezhenjie
 *
 */
public class Parameter {

	/** 参数key */
	private String key;
	/** 参数的值 */
	private String value;

	public static Parameter valueOf(String key, String value) {
		Parameter p = new Parameter();
		p.key = key;
		p.value = value;
		return p;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
