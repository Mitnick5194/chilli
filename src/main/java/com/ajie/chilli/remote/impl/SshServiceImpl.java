package com.ajie.chilli.remote.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import com.ajie.chilli.remote.ConnectConfig;
import com.ajie.chilli.remote.SshClient;
import com.ajie.chilli.remote.SshService;

/**
 *
 *
 * @author niezhenjie
 *
 */
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

	public void setBasePath(String path) {
		config.setBasePath(path);
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
		return upload(config.getBasePath(), name, stream);
	}

	@Override
	synchronized public boolean upload(String path, String name, InputStream stream)
			throws RemoteException, IOException {
		config.setBasePath(path);
		SshClient client = getClient();
		return client.upload(name, stream);
	}

	public static void main(String[] args) throws InterruptedException {
		ConnectConfig config = ConnectConfig.valueOf("ajie", "123456", "192.168.0.10", 22);
		config.setTimeout(10);
		config.setBasePath("/var/www/image/");
		config.setMax(100);
		config.setCore(1);
		final SshServiceImpl sshService = new SshServiceImpl(config);
		for (int i = 0; i < 50; i++) {
			final int j = i;
			Thread t = new Thread((i + 1) + "") {
				public void run() {
					InputStream stream;
					try {
						stream = new FileInputStream(new File(
								"C:/Users/ajie/Desktop/origin_image006.png"));
						boolean ret = sshService.upload("testimg" + (j + 1) + ".png", stream);
						System.out.println(ret);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
		}

		Thread.sleep(20000);
		SshClient client2 = sshService.getClient();
		// client2.recycle();

	}
}
