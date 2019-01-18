package com.ajie.chilli.remote;

import com.ajie.chilli.remote.exception.RemoteException;

/**
 * ssh执行任务
 *
 * @author niezhenjie
 *
 */
public interface Worker {

	void run(SessionExt session) throws RemoteException;
}
