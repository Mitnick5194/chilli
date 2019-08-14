package com.ajie.chilli.http.exception;

/**
 * 调用异常，包含网络异常和业务异常
 * 
 * @author niezhenjie
 *
 */
public class InvokeException extends Exception {

	private static final long serialVersionUID = 1L;

	/** uri语法错误 */
	public static final int TYPE_URISYNTAX = 1;

	/** 协议异常 */
	public static final int TYPE_Protocol = 1 << 1;

	/**参数异常*/
	public static final int TYPE_PARAMS = 1 << 2;

	/** 非成功状态码异常 */
	public static final int TYPE_ERRCODE = 1 << 3;

	/** 网络IO异常 */
	public static final int TYPE_IO = 1 << 4;

	/** 业务异常——结果解析失败 */
	public static final int TYPE_BUSINESS_PARSE = 1 << 5;

	/** 业务异常——返回的状态码（组织内容的码，非上述的状态码）非成功状态码 */
	public static final int TYPE_BUSINESS_ERRCODE = 1 << 6;

	/** 可提示标志 */
	public static final int MARK_CAN_TIP = 1;

	private int type;

	private int mark;

	public boolean isCanTip() {
		return (mark & MARK_CAN_TIP) == MARK_CAN_TIP;
	}

	public int getType() {
		return type;
	}

	public boolean isType(int type) {
		return (this.type & type) == type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void addType(int type) {
		this.type |= type;
	}

	public InvokeException(Exception e) {
		super(e);
	}

	public InvokeException() {
		super();
	}

	public InvokeException(String msg) {
		super(msg);
	}

	public InvokeException(String msg, int type) {
		super(msg);
		this.type = type;
	}

	public InvokeException(String msg, Exception e) {
		super(msg, e);
	}

	public InvokeException(Exception e, int type) {
		this(e);
		this.type = type;
	}

	public InvokeException(String msg, Exception e, int type) {
		this(msg, e);
		this.type = type;
	}

}
