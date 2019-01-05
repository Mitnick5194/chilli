package com.ajie.chilli.remote;

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

	/** 访问路径，绝对路径，如/var/www */
	protected String basePath;

	/** 超时值 */
	protected int timeout;

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

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getEncording() {
		return encording;
	}

	public void setEncording(String encording) {
		this.encording = encording;
	}

	public static ConnectConfig valueOf(String username, String password, String host, int port,
			String encording) {
		ConnectConfig config = new ConnectConfig();
		config.username = username;
		config.password = password;
		config.host = host;
		config.port = port;
		config.encording = encording;
		return config;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{username:").append(username).append(",");
		sb.append("password:").append(password).append(",");
		sb.append("host:").append(host).append(",");
		sb.append("port:").append(port).append(",");
		sb.append("encording:").append(encording).append("}");
		return sb.toString();
	}

	public static void main(String[] args) {
		ConnectConfig config = ConnectConfig.valueOf("ajie", "123", "www.ajie18.top", 22, "utf-8");
		System.out.println(config.toString());

	}
}
