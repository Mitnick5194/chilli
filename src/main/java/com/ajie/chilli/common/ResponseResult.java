package com.ajie.chilli.common;

/**
 * 请求响应信息封装
 * 
 * @author niezhenjie
 */
public class ResponseResult {

	/** 成功状态码 */
	public static int CODE_SUC = 200;

	/** 成功状态码 , 调用结果为空 */
	public static int CODE_NORET = 300;

	/** session过期状态吗 */
	public static final int CODE_SESSION_INVALID = 400;

	/** 失败状态，有错误或异常抛出 */
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

	public static ResponseResult success(Object data) {
		ResponseResult ret = new ResponseResult();
		ret.setCode(CODE_SUC);
		ret.setData(data);
		return ret;
	}

	public static ResponseResult error(Object data) {
		ResponseResult ret = new ResponseResult();
		ret.setCode(CODE_ERR);
		ret.setData(data);
		return ret;
	}

	public static ResponseResult empty() {
		ResponseResult ret = new ResponseResult();
		ret.setCode(CODE_NORET);
		return ret;
	}

}
