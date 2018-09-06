package com.ajie.chilli.support;

/**
 * @author niezhenjie
 */
public class OuterIdException extends Exception {

	private static final long serialVersionUID = 1L;

	public OuterIdException() {
		super();
	}

	public OuterIdException(String message) {
		super(message);
	}

	public OuterIdException(Throwable e) {
		super(e);
	}

	public OuterIdException(String message, Throwable e) {
		super(message, e);
	}

}
