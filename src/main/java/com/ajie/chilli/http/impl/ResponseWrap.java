package com.ajie.chilli.http.impl;

import com.ajie.chilli.http.Response;

public class ResponseWrap implements Response {

	private int code;

	private String msg;

	private Object data;

	public ResponseWrap(int code) {
		this.code = code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public int getStatusCode() {
		return code;
	}

	@Override
	public String getMsg() {
		return msg;
	}

	@Override
	public <T> T getData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString() {
		return "{code:" + code + ",msg:" + msg + "}";
	}

}
