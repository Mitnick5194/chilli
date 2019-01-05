package com.ajie.chilli.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.utils.common.StringUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * jsch封装
 *
 * @author niezhenjie
 *
 */
public class JSchClient {
	private final static Logger logger = LoggerFactory.getLogger(JSchClient.class);
	/** ssh连接工具 */
	private JSch jsch;

	/** 连接配置 */
	private ConnectConfig config;

	/** 连接会话 */
	private Session session;

	/** 连接通道 */
	private Channel channel;

	public JSchClient() {
		jsch = new JSch();
	}

	public JSchClient(ConnectConfig config) {
		jsch = new JSch();
		this.config = config;
	}

	public Session openSession() throws RemoteException {
		try {
			session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
			// 设置第一次登陆的时候提示，可选值：(ask | yes | no)
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(config.getPassword());
			if (config.getTimeout() <= 0) {
				session.setTimeout(10000); // 默认1s
			} else {
				session.setTimeout(config.getTimeout());
			}
			session.connect(1000);
		} catch (JSchException e) {
			logger.error("打开ssh会话失败," + config.toString(), e);
			throw new RemoteException("打开ssh会话失败," + config.toString(), e);
		}
		return session;
	}

	public Channel openChannel() throws RemoteException {
		try {
			// 创建sftp通信通道
			channel = session.openChannel("sftp");
			channel.connect(1000);
		} catch (JSchException e) {
			logger.error("打开ssh会话失败," + config.toString(), e);
			throw new RemoteException("打开ssh会话失败," + config.toString(), e);
		}
		return channel;
	}

	public boolean upload(String fileName, InputStream stream) throws RemoteException, IOException {
		openSession();
		String path = createFolders();
		ChannelSftp sftp = (ChannelSftp) openChannel();
		OutputStream out = null;
		try {
			out = sftp.put(path + fileName);
			byte[] buf = new byte[1024];
			int n = stream.read(buf);
			while (n != -1) {
				out.write(buf, 0, n);
				n = stream.read(buf);
			}
			out.flush();
		} catch (SftpException e) {
			logger.error("无法创建目录 ", path, e);
			throw new RemoteException("无法创建目录", e);
		} finally {
			out.close();
			stream.close();
			sftp.disconnect();
			session.disconnect();
		}
		return true;
	}

	/**
	 * 切割配置里的目录路径 basePath形式 如：/var/www/或var/www 不管哪种形式，都是绝对路径
	 * 
	 * @return
	 * @throws RemoteException
	 */
	private String createFolders() throws RemoteException {
		String basepath = config.getBasePath();
		if (basepath.startsWith("/")) {
			basepath = basepath.substring(1);
		}
		String[] folders = basepath.split("/");
		if (null == folders) {
			folders = new String[0];
		}
		String path = "";
		ChannelSftp sftp = (ChannelSftp) openChannel();
		// 进入目录，如果目录不存在，则创建目录
		for (int i = 0; i < folders.length; i++) {
			path += "/" + folders[i];
			boolean currErr = false;// 创建目录过程中出现了错误
			Throwable e = null;
			try {
				sftp.cd(path);
			} catch (SftpException exce) {
				// 没有则创建
				try {
					sftp.mkdir(path);
				} catch (SftpException e1) {
					currErr = true;
					e = e1;
				}
			}
			if (currErr) {
				logger.error("无法创建目录 ", path, e);
				throw new RemoteException("无法创建目录 ", e);
			}
		}
		// 结尾加上/如/var/www/
		if (!StringUtils.isEmpty(path)) {
			path += "/";
		}
		return path;
	}

	static public JSchClient getClient(ConnectConfig config) {
		return new JSchClient(config);
	}

	public static void main(String[] args) {
		ConnectConfig config = ConnectConfig.valueOf("ajie", "niezhenjie22", "www.ajie18.top", 22,
				"utf-8");
		config.setBasePath("/var/www/image/");
		JSchClient client = JSchClient.getClient(config);
		InputStream stream;
		try {
			stream = new FileInputStream(new File("C:/Users/ajie/Desktop/origin_image006.png"));
			boolean ret = client.upload("testimg.png", stream);
			System.out.println(ret);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}
}
