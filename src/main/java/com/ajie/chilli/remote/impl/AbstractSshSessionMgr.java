package com.ajie.chilli.remote.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.ajie.chilli.remote.ConnectConfig;
import com.ajie.chilli.remote.SessionExt;
import com.ajie.chilli.remote.SshSessionMgr;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * ssh会话管理工具抽象类
 *
 * @author niezhenjie
 *
 */
public abstract class AbstractSshSessionMgr implements SshSessionMgr {
	/** 默认的名字前缀，可通过传入的值修改biz替换默认 */
	public static final String DEFAULT_NAME_PREFIX = "asyn-ssh-";

	/** 重连失败最大次数 */
	public final static int MAX_RETRY_COUNT = 3;

	/** ssh连接工具 */
	protected JSch jsch;

	/** 连接配置 */
	protected ConnectConfig config;

	/** 业务类型，可以作为标识 */
	protected String biz;

	/** session连接池 */
	volatile protected List<SessionExt> sessionPool = Collections.emptyList();

	public AbstractSshSessionMgr(ConnectConfig config) {
		this(config, DEFAULT_NAME_PREFIX);
	}

	public AbstractSshSessionMgr(ConnectConfig config, String biz) {
		jsch = new JSch();
		this.config = config;
		sessionPool = new ArrayList<SessionExt>(config.getMax());
		this.biz = biz;
		recycleWatch();
	}

	abstract public Logger getLogger();

	/**
	 * 获取jsch
	 * 
	 * @return
	 */
	@Override
	public JSch getJSch() {
		return jsch;
	}

	/**
	 * 获取连接配置
	 * 
	 * @return
	 */
	public ConnectConfig getConfig() {
		return config;
	}

	public void setBiz(String biz) {
		this.biz = biz;
	}

	/**
	 * 回收会话
	 */
	@Override
	public void recycleWatch() {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				recycle();
				// getLogger().info(getInfo());
			}
		};
		ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor(new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setName("ssh-recycle-thread");
						return t;
					}
				});
		// service.scheduleAtFixedRate(run, 1, 1, TimeUnit.MINUTES);// 3min
		service.scheduleAtFixedRate(run, 90, 90, TimeUnit.SECONDS);// 3min

	}

	/**
	 * 回收session，值保留core个
	 */
	@Override
	public void recycle() {
		synchronized (sessionPool) {
			for (int i = config.getCore(); i < config.getMax(); i++) {
				SessionExt session = sessionPool.get(i);
				if (!session.isIdle()) // 只回收空闲的
					continue;
				session.setState(SessionExt.STATE_DESTORING);
				session.recycle();
				session.setState(SessionExt.STATE_DESTORIED);
				if (getLogger().isTraceEnabled()) {
					getLogger().info(
							Thread.currentThread().getName() + "正在回收ssh连接 " + session.toString(),
							"current sessionPool size:" + sessionPool.size() + " core:"
									+ config.getCore());
				}
			}
		}
	}

	/**
	 * 获取一个会话，将会话的状态改为运行中并放入会话池
	 * 
	 * @return
	 */
	@Override
	public SessionExt getSession() {
		synchronized (sessionPool) {
			for (SessionExt session : sessionPool) {
				if (session.isIdle()) {
					// 有空闲的会话，直接使用
					session.setState(SessionExt.STATE_ACTIVE);
					return session;
				}
			}
			if (sessionPool.size() < config.getMax()) {
				// 会话创建数还没有达到最大值，创建
				SessionExt session = openSession();
				session.active();
				sessionPool.add(session);
				return session;
			}
		}
		// 连接池全忙，根据子类的实现做响应的处理
		return null;
	}

	/**
	 * 打开一个会话，状态为空闲
	 * 
	 * @return
	 * @throws RemoteException
	 */
	@Override
	public SessionExt openSession() {
		Session session = null;
		try {
			session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
			// 设置第一次登陆的时候提示，可选值：(ask | yes | no)
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(config.getPassword());
			if (config.getTimeout() <= 0) {
				session.setTimeout(5000); // 默认5s
			} else {
				session.setTimeout(config.getTimeout());
			}
			session.connect(config.getTimeout());
			return new SessionExt(session, biz + (this.sessionPool.size() + 1));
		} catch (JSchException e) {
			if (null != session) {
				if (session.isConnected())
					session.disconnect();
				session = null;
			}
			getLogger().error("打开ssh会话失败," + config.toString(), e);
		}
		return null;
	}

	// abstract public String getInfo();

}
