package com.ajie.chilli.remote.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.remote.ConnectConfig;
import com.ajie.chilli.remote.SessionExt;
import com.ajie.chilli.remote.SshSessionMgr;
import com.ajie.chilli.remote.UploadService;
import com.ajie.chilli.remote.Worker;
import com.ajie.chilli.remote.exception.RemoteException;
import com.ajie.chilli.utils.common.StringUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * 文件上传
 *
 * @author niezhenjie
 *
 */
public class UploadServiceImpl implements UploadService {
	private final static Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);

	/** 连接会话 */
	private SshSessionMgr sessionMgr;

	public UploadServiceImpl(SshSessionMgr sessionMgr) {
		this.sessionMgr = sessionMgr;
	}

	public SshSessionMgr getSshSessionMgr() {
		return sessionMgr;
	}

	public void setSshSessionMgr(SshSessionMgr session) {
		this.sessionMgr = session;
	}

	@Override
	public void upload(InputStream stream, String fileName) throws RemoteException {
		upload(stream, DEFAULT_PATH, fileName);
	}

	@Override
	public void upload(InputStream stream, String path, String fileName) throws RemoteException {
		if (null == stream)
			throw new RemoteException("无效输入流");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		try {
			int n = stream.read(buf);
			while (n > -1) {
				out.write(buf, 0, n);
				n = stream.read(buf);
			}
			upload(out.toByteArray(), path, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
				out.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void upload(byte[] stream, String fileName) throws RemoteException {
		upload(stream, DEFAULT_PATH, fileName);
	}

	@Override
	public void upload(final byte[] stream, final String path, final String fileName)
			throws RemoteException {
		Worker worker = new Worker() {
			@Override
			public void run(SessionExt session) throws RemoteException {
				Channel channel;
				try {
					channel = session.openChannel(3000, "sftp");
					ChannelSftp sftp = (ChannelSftp) channel;
					String folder = createFolders(path, sftp);
					OutputStream out = sftp.put(folder + fileName);
					out.write(stream);
					out.flush();
					out.close();
				} catch (JSchException e) {
					logger.error("文件上传失败", e);
					throw new RemoteException("上传文件失败", e);
				} catch (SftpException e) {
					logger.error("文件上传失败", e);
					throw new RemoteException();
				} catch (IOException e) {
					logger.error("文件上传失败", e);
					throw new RemoteException();
				}
			}
		};
		sessionMgr.execute(worker);
	}

	/**
	 * 切割配置里的目录路径 basePath形式 如：/var/www/或var/www 不管哪种形式，都是绝对路径
	 * 
	 * @return
	 * @throws RemoteException
	 */
	private String createFolders(String path, ChannelSftp sftp) throws RemoteException {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String[] folders = path.split("/");
		if (null == folders) {
			folders = new String[0];
		}
		String cd = "";
		// 进入目录，如果目录不存在，则创建目录
		for (int i = 0; i < folders.length; i++) {
			cd += "/" + folders[i];
			boolean currErr = false;// 创建目录过程中出现了错误
			Throwable e = null;
			try {
				sftp.cd(cd);
			} catch (SftpException exce) {
				// 没有则创建
				try {
					sftp.mkdir(cd);
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
			cd += "/";
		}
		return cd;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Properties prop = new Properties();
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("server.properties");
		prop.load(is);
		String host = prop.getProperty("host");
		String passwd = prop.getProperty("passwd");
		String name = prop.getProperty("name");
		ConnectConfig config = ConnectConfig.valueOf(name, passwd, host, 22);
		// timeout一般来说需要设置大一点，否则会出现各种超时
		config.setTimeout(3000);
		config.setMax(10);
		config.setCore(0);
		// final AsynSshSessionMgr mgr = new AsynSshSessionMgr(config);
		final SshSessionMgr mgr = new AsynSshSessionMgr(config);
		final UploadService upload = new UploadServiceImpl(mgr);
		for (int i = 0; i < 10; i++) {
			final int j = i;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						InputStream stream = new FileInputStream(new File(
								"C:/Users/ajie/Desktop/103264437383964562.jpg"));
						upload.upload(stream, "/var/www/image", "bigimg" + j + ".png");
					} catch (RemoteException e) {

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
		}

	}
}
