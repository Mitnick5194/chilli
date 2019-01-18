package com.ajie.chilli.remote;

import com.ajie.chilli.remote.exception.RemoteException;
import com.jcraft.jsch.JSch;

/**
 * ssh会话管理接口
 *
 * @author niezhenjie
 *
 */
public interface SshSessionMgr {

	/** 等待队列默认最大值 */
	public final static int DEFAULT_WAIT_SIZE = 10;

	/** 默认超时值 -- 5s */
	public final static int DEFAULT_TIME_OUT = 5000;

	/** 重连失败最大次数 */
	public final static int MAX_RETRY_COUNT = 3;

	/**
	 * 获取jsch
	 * 
	 * @return
	 */
	public JSch getJSch();

	/**
	 * 打开一个会话，状态为空闲
	 * 
	 * @return
	 * @throws RemoteException
	 */
	SessionExt openSession() throws RemoteException;

	/**
	 * 获取一个会话，将会话的状态改为运行中并放入会话池
	 * 
	 * @return
	 */
	SessionExt getSession() throws RemoteException;

	/**
	 * 执行任务
	 * 
	 * @param worker
	 * @throws RemoteException
	 */
	void execute(Worker worker) throws RemoteException;

	/**
	 * 回收会话监听
	 */
	void recycleWatch() throws RemoteException;

	/**
	 * 回收session，值保留core个
	 */
	void recycle() throws RemoteException;

}
