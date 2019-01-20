package com.ajie.chilli.picture;

/**
 * 远程模块异常类
 *
 * @author niezhenjie
 *
 */
public class PictureException extends Exception {

	private static final long serialVersionUID = 1L;

	public PictureException() {
		super();
	}

	public PictureException(String msg) {
		super(msg);
	}

	public PictureException(String msg, Throwable e) {
		super(msg, e);
	}

	public PictureException(Throwable e) {
		super(e);
	}

}
