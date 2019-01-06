package com.ajie.chilli.remote;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * jsch session封装
 *
 * @author niezhenjie
 *
 */
public class SessionExt {

	private final static Logger logger = LoggerFactory.getLogger(SessionExt.class);

	/** 是否允许trace */
	protected final static boolean _TraceEnabled = logger.isTraceEnabled();

	/** 连接活跃状态 */
	public static final int STATE_ACTIVE = 0x100;
	/** 空闲状态 */
	public static final int STATE_IDLE = 0x1000;

	/** ssh会话 */
	private Session session;
	/** 状态 */
	private int state;

	private String name;

	/** 打开channel重试次数 */
	private AtomicInteger retryCount = new AtomicInteger();

	public SessionExt(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

	public int state() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void idle() {
		this.state = STATE_IDLE;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public boolean isActive() {
		return STATE_ACTIVE == state;
	}

	public boolean isIdle() {
		return STATE_IDLE == state;
	}

	public void disconnect() {
		session.disconnect();
	}

	public void recycle() {
		disconnect();
		session = null;
	}

	/**
	 * 打开一个通道，如果已经打开过并且没有关闭连接（手动关闭模式），则直接使用
	 * 
	 * @return
	 * @throws JSchException
	 * @throws RemoteException
	 */
	public Channel openChannel(int timeout, String type) throws JSchException {
		Session session = this.session;
		// 创建sftp通信通道
		Channel channel = session.openChannel(type);
		channel.connect(timeout);
		return channel;
	}

}
