package com.ajie.chilli.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
 *
 * @author niezhenjie
 *
 */
public class SshClient {
	private final static Logger logger = LoggerFactory.getLogger(SshClient.class);
	/** 是否允许trace */
	protected final static boolean _TraceEnabled = logger.isTraceEnabled();

	/** 关闭通道和session模式 -- 提交一次指令后立刻关闭 */
	/*
	public final static int MODE_COMMITTED_CLOSE = 0x100;

	*//** 关闭通道和session模式 -- 提交一次指令后不关闭，需手动调用close关闭 */
	/*
	public final static int MODE_DELAY_CLOSE = 0x1000;*/

	/** 重连失败最大次数 */
	public final static int MAX_RETRY_COUNT = 3;

	/** ssh连接工具 */
	private JSch jsch;

	/** 连接配置 */
	private ConnectConfig config;

	/** 关闭通道和session模式，默认立刻关闭 */
	private int closeMode;

	/** 存放连接 queue的数量就是空闲session的数量，因为有任务的session会从队列中取出，执行完后放回队列 */
	BlockingQueue<SessionExt> queue;

	/** 重连次数 */
	AtomicInteger retryCount = new AtomicInteger();

	/** 当前创建的连接数 */
	AtomicInteger count = new AtomicInteger();

	public SshClient() {
		jsch = new JSch();
	}

	public SshClient(ConnectConfig config) {
		jsch = new JSch();
		this.config = config;
		queue = new ArrayBlockingQueue<>(config.getMax());
		init();
	}

	public JSch getJSch() {
		return jsch;
	}

	public ConnectConfig getConfig() {
		return config;
	}

	public void setCloseMode(int closeMode) {
		this.closeMode = closeMode;
	}

	public int getCloseMode() {
		return closeMode;
	}

	/*	public boolean isCommittedCloseMode() {
			return closeMode == MODE_COMMITTED_CLOSE;
		}

		public boolean isDelayCloseMode() {
			return closeMode == MODE_DELAY_CLOSE;
		}*/

	private void init() {
		synchronized (queue) {
			int core = config.getCore();
			for (int i = 0; i < core; i++) {
				SessionExt session = openSession();
				if (null == session) {
					continue;
				}
				queue.offer(session);
				count.incrementAndGet();
			}
		}
		recycleWatch();
	}

	/**
	 * 回收监听线程
	 */
	public void recycleWatch() {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				recycle();
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
		service.scheduleAtFixedRate(run, 10, 10, TimeUnit.SECONDS);// 30s

	}

	public SessionExt getSession() throws RemoteException {
		SessionExt session = null;
		synchronized (queue) {
			while (queue.size() == 0 && count.get() == config.getMax()) {
				// 队列中没有可用的会话，并且当前创建的会话数是已经达到最大数，阻塞等待
				try {
					queue.wait();
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
			try {
				// 队列还有，直接取吧
				if (queue.size() != 0) {
					session = queue.take();
				} else {
					// 队列没有，那么上面的queue.zise为true，后面的就肯定为false了，直接创建
					session = openSession();
					putSessionToQueue(session);
				}
			} catch (InterruptedException e) {
				logger.error("", e);
			} finally {
				queue.notifyAll();
			}
		}
		return session;
	}

	private void putSessionToQueue(SessionExt session) {
		if (null == session)
			return;
		synchronized (queue) {
			try {
				queue.put(session);
				count.incrementAndGet();
			} catch (InterruptedException e) {
				// 添加失败,直接回收
				session.recycle();
			}

		}
	}

	/**
	 * 运行完毕，将会话放回队列
	 * 
	 * @param session
	 */
	public void putIntoQueue(SessionExt session) {
		synchronized (queue) {
			try {
				queue.put(session);
			} catch (InterruptedException e) {
				logger.error("", e);
			} finally {
				queue.notifyAll();// 唤醒等待线程
			}
		}
	}

	/**
	 * 回收session，值保留core个
	 */
	public void recycle() {
		synchronized (queue) {
			try {
				for (int i = 0, len = queue.size(); i < len; i++) {
					if (queue.size() > config.getCore()) {
						SessionExt session = queue.take();
						session.recycle();
						session = null;
						count.decrementAndGet();
						logger.info("执行ssh连接回收,queue大小 " + queue.size() + " core:"
								+ config.getCore() + " count " + count.get());
					} else {
						break;
					}
				}
			} catch (InterruptedException e) {
				logger.error("", e);
			} finally {
				queue.notifyAll();
			}
		}
	}

	/**
	 * 打开一个会话，如果已经打开过并且没有关闭连接（手动关闭模式），则直接使用
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public SessionExt openSession() {
		Session session = null;
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
			session.connect(config.getTimeout());
			SessionExt sessionExt = new SessionExt(session);
			return sessionExt;
		} catch (JSchException e) {
			/*
			 * TODO 重连机制 retryCount全局的，所有线程共享，不能这样做，到时在想办法实现吧，先不重连了
			 * if (MAX_RETRY_COUNT <= retryCount.incrementAndGet()) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					logger.error("", e1);
				}
				if (_TraceEnabled) {
					logger.info("ssh第" + retryCount.get() + "次尝试重连");
				}
				return openSession();
			}*/
			if (null != session) {
				if (session.isConnected())
					session.disconnect();
				session = null;
			}
			logger.error("打开ssh会话失败," + config.toString(), e);
		}
		return null;
	}

	/**
	 * 上传文件
	 * 
	 * @param fileName
	 *            文件名
	 * @param stream
	 *            文件流
	 * @return
	 * @throws RemoteException
	 * @throws IOException
	 */
	/**
	 * @param fileName
	 * @param stream
	 * @return
	 * @throws RemoteException
	 * @throws IOException
	 */
	public boolean upload(String fileName, InputStream stream) throws RemoteException, IOException {
		// long start = System.currentTimeMillis();
		SessionExt session = getSession();
		Channel channel = null;
		if (null == session) {
			throw new RemoteException("网络异常，请稍后再试");
		}
		OutputStream out = null;
		String path = "";
		try {
			channel = session.openChannel(config.getTimeout(), "sftp");
			ChannelSftp sftp = (ChannelSftp) channel;
			path = createFolders(sftp);
			out = sftp.put(path + fileName);
			byte[] buf = new byte[1024];
			int n = stream.read(buf);
			while (n != -1) {
				out.write(buf, 0, n);
				n = stream.read(buf);
			}
			out.flush();
			if (_TraceEnabled) {
				logger.info("文件上传至服务器，" + config.toString());
			}
		} catch (SftpException e) {
			logger.error("无法创建目录 ", path, e);
			throw new RemoteException("无法创建目录", e);
		} catch (JSchException e) {
			logger.error("无法打开channel ", e);
		} finally {
			if (null != out)
				out.close();
			stream.close();
			putIntoQueue(session);// 放回队列
			channel.disconnect();
		}
		// System.out.println(System.currentTimeMillis() - start);
		return true;
	}

	/**
	 * 切割配置里的目录路径 basePath形式 如：/var/www/或var/www 不管哪种形式，都是绝对路径
	 * 
	 * @return
	 * @throws RemoteException
	 */
	private String createFolders(ChannelSftp sftp) throws RemoteException {
		String basepath = config.getBasePath();
		if (basepath.startsWith("/")) {
			basepath = basepath.substring(1);
		}
		String[] folders = basepath.split("/");
		if (null == folders) {
			folders = new String[0];
		}
		String path = "";
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

	static public SshClient getClient(ConnectConfig config) {
		return new SshClient(config);
	}

	public String toString() {
		return config.toString();
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
		config.setMax(5);
		config.setCore(2);
		config.setBasePath("/var/www/image/");
		SshClient client = SshClient.getClient(config);
		// client.setCloseMode(MODE_DELAY_CLOSE);
		InputStream stream;
		InputStream stream2;
		try {
			stream = new FileInputStream(new File("C:/Users/ajie/Desktop/origin_image006.png"));
			stream2 = new FileInputStream(new File("C:/Users/ajie/Desktop/arrow_top.png"));
			boolean ret = client.upload("testimg.png", stream);
			boolean ret2 = client.upload("arrow.png", stream2);
			System.out.println(ret);
			System.out.println(ret2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
