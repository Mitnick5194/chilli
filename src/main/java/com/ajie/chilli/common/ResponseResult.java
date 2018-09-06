package com.ajie.chilli.common;

/**
 * 请求响应信息封装
 * 
 * @author niezhenjie
 */
public class ResponseResult {

	/** 成功状态码 */
	public static int CODE_SUC = 200;
	/** 失败状态 */
	public static int CODE_ERR = 500;

	/**
	 * 状态码
	 */
	protected int code;

	/**
	 * 返回消息
	 */
	protected String msg;

	/**
	 * 返回数据
	 */
	protected Object data;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public static ResponseResult newResult(int code, String msg, Object data) {
		ResponseResult ret = new ResponseResult();
		ret.setCode(code);
		ret.setMsg(msg);
		ret.setData(data);
		return ret;
	}

	public static ResponseResult newResult(int code, String msg) {
		ResponseResult ret = new ResponseResult();
		ret.setCode(code);
		ret.setMsg(msg);
		return ret;
	}

	public static ResponseResult newResult(int code, Object data) {
		ResponseResult ret = new ResponseResult();
		ret.setCode(code);
		ret.setData(data);
		return ret;
	}

}
