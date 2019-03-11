package com.ajie.chilli.mail;

/**
 * 邮件服务异常类
 *
 * @author niezhenjie
 *
 */
public class MailException extends Exception {

	private static final long serialVersionUID = 1L;

	public MailException() {
		super();
	}

	public MailException(String msg) {
		super(msg);
	}

	public MailException(String msg, Throwable e) {
		super(msg, e);
	}

	public MailException(Throwable e) {
		super(e);
	}

}
