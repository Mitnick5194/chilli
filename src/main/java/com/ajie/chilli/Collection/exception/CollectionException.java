package com.ajie.chilli.collection.exception;

/**
 * @author niezhenjie
 */
public class CollectionException extends Exception {

	private static final long serialVersionUID = 1L;

	public CollectionException() {
		super();
	}

	public CollectionException(String message) {
		super(message);
	}

	public CollectionException(Throwable e) {
		super(e);
	}

	public CollectionException(String message, Throwable e) {
		super(message, e);
	}

}
