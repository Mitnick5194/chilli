package com.ajie.chilli.cache.redis;

/**
 * @author niezhenjie
 */
public class RedisException extends Exception {

	private static final long serialVersionUID = 1L;

	public RedisException() {
		super();
	}

	public RedisException(String message) {
		super(message);
	}

	public RedisException(Throwable e) {
		super(e);
	}

	public RedisException(String messge, Throwable e) {
		super(messge, e);
	}

}
