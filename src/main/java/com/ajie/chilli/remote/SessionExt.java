package com.ajie.chilli.remote;

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

	/** 连接活跃状态 */
	public static final int STATE_ACTIVE = 1 << 3;
	/** 空闲状态 */
	public static final int STATE_IDLE = 1 << 0;
	/** 正在销毁 */
	public static final int STATE_DESTORING = 1 << 1;
	/** 已经销毁 */
	public static final int STATE_DESTORIED = 1 << 2;

	/** ssh会话 */
	private Session session;
	/** 状态 */
	volatile private int state;
	/** 名字 */
	private String name;
	/** 通道 */
	volatile private Channel channel;

	/** 打开channel重试次数 */
	// private AtomicInteger retryCount = new AtomicInteger();

	public SessionExt(Session session) {
		this.session = session;
		state = STATE_IDLE;
	}

	public SessionExt(Session session, String name) {
		this.session = session;
		state = STATE_IDLE;
		this.name = name;
	}

	/**
	 * 获取session
	 * 
	 * @return
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * 获取当前状态
	 * 
	 * @return
	 */
	public int state() {
		return state;
	}

	/**
	 * 设置状态
	 * 
	 * @param state
	 */
	public void setState(int state) {
		this.state = state;
	}

	/**
	 * 将状态设为空闲并关闭channel
	 */
	public void idle() {
		/*if (null != channel && channel.isConnected())
			channel.disconnect();*/
		this.state = STATE_IDLE;
	}

	/**
	 * 将状态设为活跃（使用）
	 */
	public void active() {
		this.state = STATE_ACTIVE;
	}

	/**
	 * 设置session名
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取session名
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 是否为活跃（正在使用）状态
	 * 
	 * @return
	 */
	public boolean isActive() {
		return STATE_ACTIVE == state;
	}

	/**
	 * 是否为空闲状态
	 * 
	 * @return
	 */
	public boolean isIdle() {
		return STATE_IDLE == state;
	}

	/**
	 * 断开session连接
	 */
	public void disconnect() {
		if (!session.isConnected())
			return;
		session.disconnect();
	}

	/**
	 * 断开连接并回收置空session
	 */
	public void recycle() {
		disconnectChannel();
		disconnect();
		session = null;
	}

	/**
	 * 打开一个指定类型的突破到
	 * 
	 * @param timeout
	 *            超时值
	 * @param type
	 *            类型
	 * @return
	 * @throws JSchException
	 */
	public Channel openChannel(int timeout, String type) throws JSchException {
		Session session = this.session;
		// 创建sftp通信通道
		Channel channel = session.openChannel(type);
		channel.connect(timeout);
		this.channel = channel;
		return channel;
	}

	/**
	 * 打开一个指定类型的突破到
	 * 
	 * @param type
	 *            类型
	 * @return
	 * @throws JSchException
	 */
	public Channel openChannel(String type) throws JSchException {
		return openChannel(30 * 1000, type);
	}

	public void disconnectChannel() {
		if (null == channel)
			return;
		if (!channel.isConnected())
			return;
		channel.disconnect();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name:").append(name).append(",");
		sb.append("state:").append(state).append("}");
		return sb.toString();
	}

}
