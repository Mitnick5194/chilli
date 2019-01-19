package com.ajie.chilli.remote.impl;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.remote.ConnectConfig;
import com.ajie.chilli.remote.SessionExt;
import com.ajie.chilli.remote.Worker;
import com.ajie.chilli.remote.exception.RemoteException;

/**
 * 阻塞等待会话
 *
 * @author niezhenjie
 *
 */
public class BlockSshSessionMgr extends AbstractSshSessionMgr {

	private final static Logger logger = LoggerFactory.getLogger(BlockSshSessionMgr.class);

	/** 锁 */
	private Lock lock = new ReentrantLock();
	Condition condition = lock.newCondition();

	public BlockSshSessionMgr(ConnectConfig config) {
		super(config);
	}

	public BlockSshSessionMgr(ConnectConfig config, String biz) {
		super(config, biz);
	}

	@Override
	public void execute(Worker worker) throws RemoteException {
		SessionExt session = null;
		lock.lock();
		// 阻塞等待，知道有可用的session
		while (null == (session = getSession())) {
			try {
				condition.await();
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
		lock.unlock();
		run(worker, session);

	}

	private void run(Worker worker, SessionExt session) throws RemoteException {
		if (null == worker)
			throw new RemoteException("缺少任务");
		if (null == session)
			throw new RemoteException("打开session会话失败");
		try {
			worker.run(session);
		} catch (Exception e) {
			logger.error("任务执行失败", e);
			throw new RemoteException("任务执行失败", e);
		}
		try {
			lock.lock();
			session.disconnectChannel();
			session.idle();
		} finally {
			condition.signalAll();// 唤醒对象的等待线程
			lock.unlock();
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
