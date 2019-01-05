package com.ajie.chilli.remote.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.rmi.RemoteException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.remote.ConnectConfig;
import com.ajie.chilli.remote.JschService;

/**
 *
 *
 * @author niezhenjie
 *
 */
public class FTPClientServiceImpl implements JschService {

	private final static Logger logger = LoggerFactory.getLogger(FTPClientServiceImpl.class);

	/** 是否允许trace */
	protected final static boolean _TraceEnabled = logger.isTraceEnabled();

	/** 连接配置 */
	private ConnectConfig config;

	public FTPClientServiceImpl(String host, int port, String username, String password) {
		ConnectConfig config = ConnectConfig.valueOf(username, password, host, port, "utf-8");
		this.config = config;
	}

	public void setBasePath(String path) {
		config.setBasePath(path);
	}

	public void setTimeout(int timeout) {
		config.setTimeout(timeout);
	}

	@Override
	public boolean upload(InputStream stream) throws RemoteException {
		return upload(stream, config.getBasePath());
	}

	@Override
	public boolean upload(InputStream stream, String name) throws RemoteException {
		FTPClient client = getClient();
		return false;
	}

	@Override
	public FTPFile[] listFiles() throws RemoteException {
		FTPClient client = getClient();
		FTPFile[] listFiles = null;
		try {
			listFiles = client.listFiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (FTPFile f : listFiles) {
			System.out.println(f.getName());
		}
		return listFiles;
	}

	private FTPClient getClient() throws RemoteException {
		FTPClient client = new FTPClient();
		client.setControlEncoding(config.getEncording());
		client.setConnectTimeout(config.getTimeout());
		try {
			client.connect(config.getHost(), config.getPort());
		} catch (SocketException e) {
			logger.error("ftp连接错误", e);
			throw new RemoteException("ftp连接错误", e);
		} catch (IOException e) {
			logger.error("ftp连接错误", e);
			throw new RemoteException("ftp连接错误", e);
		}
		if (_TraceEnabled) {
			logger.info("ftp连接成功," + config.toString());
		}
		return client;
	}
	

	public static void main(String[] args) {
		FTPClientServiceImpl service = new FTPClientServiceImpl("47.106.211.15", 21, "root",
				"Ajie@zq-root159");
		//service.setBasePath("/var/www");
		service.setTimeout(1200000);
		try {
			service.listFiles();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
