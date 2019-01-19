package com.ajie.chilli.remote.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;

import com.ajie.chilli.remote.ConnectConfig;
import com.ajie.chilli.remote.SshClient;
import com.ajie.chilli.remote.SshService;

/**
 * ssh服务实现
 *
 * @author niezhenjie
 * 
 */
@SuppressWarnings("deprecation")
public class SshServiceImpl implements SshService {

	// private final static Logger logger =
	// LoggerFactory.getLogger(SshServiceImpl.class);

	/** 连接配置 */
	private ConnectConfig config;

	/** ssh客户端 */

	private SshClient client;

	public SshServiceImpl() {

	}

	public SshServiceImpl(ConnectConfig config) {
		this.config = config;
		client = SshClient.getClient(config);
	}

	public SshServiceImpl(String host, String username, String password) {
		this(host, DEFAULT_PORT, username, password);
	}

	public SshServiceImpl(String host, int port, String username, String password) {
		ConnectConfig config = ConnectConfig.valueOf(username, password, host, port);
		this.config = config;
		client = SshClient.getClient(config);
	}

	public void setTimeout(int timeout) {
		config.setTimeout(timeout);
	}

	public SshClient getClient() {
		return client;
	}

	public void setSshClient(SshClient client) {
		this.client = client;
	}

	@Override
	public boolean upload(String name, InputStream stream) throws IOException {
		return upload(DEFAULT_PATH, name, stream);
	}

	@Override
	synchronized public boolean upload(String path, String name, InputStream stream)
			throws RemoteException, IOException {
		SshClient client = getClient();
		return client.upload(path, name, stream);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		Properties prop = new Properties();
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("server.properties");
		prop.load(is);
		String host = prop.getProperty("host");
		String passwd = prop.getProperty("passwd");
		String name = prop.getProperty("name");
		ConnectConfig config = ConnectConfig.valueOf(name, passwd, host, 22);
		config.setTimeout(1000);
		config.setMax(15);
		config.setCore(1);
		final SshServiceImpl sshService = new SshServiceImpl(config);
		for (int i = 0; i < 1; i++) {
			final int j = i;
			Thread t = new Thread((i + 1) + "") {
				public void run() {
					InputStream stream;
					try {
						stream = new FileInputStream(
								new File("C:/Users/ajie/Desktop/arrow_top.png"));
						boolean ret = sshService.upload("testimg" + (j + 1) + ".png", stream);
						System.out.println(ret);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
		}

		// SshClient client2 = sshService.getClient();
		// client2.recycle();

	}
}
