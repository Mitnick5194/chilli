package com.ajie.chilli.remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 远程登录服务器所需的基本配置信息
 * 
 * @author niezhenjie
 *
 */
public class ConnectConfig {

	/** 登录用户名 */
	protected String username;

	/** 密码 */
	protected String password;

	/** 服务器地址 */
	protected String host;

	/** 字符编码 */
	protected String encording;

	/** 端口 */
	protected int port;

	/** 超时值 */
	protected int timeout;

	/** 会话核心连接数 */
	protected int core;

	/** 会话最大连接数 */
	protected int max;

	/** 空闲连接（max-core)存活时间 单位ms */
	protected int keepAliveTime;

	/** 任务池大小 */
	protected int workerQueueSize;

	public ConnectConfig() {
		timeout = SshSessionMgr.DEFAULT_TIME_OUT;
		workerQueueSize = SshSessionMgr.DEFAULT_WAIT_SIZE;
		encording = "utf-8";
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getEncording() {
		return encording;
	}

	public void setEncording(String encording) {
		this.encording = encording;
	}

	public void setCore(int core) {
		this.core = core;
	}

	public int getCore() {
		return core;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMax() {
		return max;
	}

	public void setKeepAliveTime(int keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public int getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setWorkerQueueSize(int size) {
		this.workerQueueSize = size;
	}

	public int getWorkerQueueSize() {
		return workerQueueSize;
	}

	public static ConnectConfig valueOf(String username, String password, String host, int port) {
		ConnectConfig config = new ConnectConfig();
		config.username = username;
		config.password = password;
		config.host = host;
		config.port = port;
		return config;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{username:").append(username).append(",");
		sb.append("password:").append(password).append(",");
		sb.append("host:").append(host).append(",");
		sb.append("port:").append(port).append(",");
		sb.append("encording:").append(encording).append(",");
		sb.append("timeout:").append(timeout).append(",");
		sb.append("core:").append(core).append(",");
		sb.append("max:").append(max).append(",");
		sb.append("keepAliveTime:").append(keepAliveTime).append("}");
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		Properties prop = new Properties();
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("server.properties");
		prop.load(is);
		String host = prop.getProperty("host");
		String passwd = prop.getProperty("passwd");
		String name = prop.getProperty("name");
		ConnectConfig config = ConnectConfig.valueOf(name, passwd, host, 22);
		System.out.println(config.toString());
	}
}
