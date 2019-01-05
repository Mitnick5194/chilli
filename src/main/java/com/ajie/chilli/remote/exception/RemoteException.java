package com.ajie.chilli.remote.exception;

/**
 * 远程模块异常类
 *
 * @author niezhenjie
 *
 */
public class RemoteException extends Exception {

	private static final long serialVersionUID = 1L;

	public RemoteException() {
		super();
	}

	public RemoteException(String msg) {
		super(msg);
	}

	public RemoteException(String msg, Throwable e) {
		super(msg, e);
	}

	public RemoteException(Throwable e) {
		super(e);
	}

}
