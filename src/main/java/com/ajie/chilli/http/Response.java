package com.ajie.chilli.http;

/**
 * 相应信息
 * 
 * @author niezhenjie
 *
 */
public interface Response {

	/** 状态码 */
	int getStatusCode();

	/** 信息 */
	String getMsg();

	/** 数据 */
	<T> T getData();

}
