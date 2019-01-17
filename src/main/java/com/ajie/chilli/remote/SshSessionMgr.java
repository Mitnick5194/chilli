package com.ajie.chilli.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * ssh会话管理
 *
 * @author niezhenjie
 *
 */
public class SshSessionMgr {

	private final static Logger logger = LoggerFactory.getLogger(SshSessionMgr.class);
	/** 是否允许trace */
	protected final static boolean _TraceEnabled = logger.isTraceEnabled();

	/** 默认的名字前缀，可通过传入的值修改biz替换默认 */
	public static final String DEFAULT_NAME_PREFIX = "ssh-";

	/** 等待队列最大值 */
	public final static int WAIT_SIZE = 10;

	/** 重连失败最大次数 */
	public final static int MAX_RETRY_COUNT = 3;

	/** ssh连接工具 */
	private JSch jsch;

	/** 连接配置 */
	private ConnectConfig config;

	/** 业务类型，可以作为标识 */
	private String biz;

	/** session连接池 */
	volatile private List<SessionExt> sessionPool = Collections.emptyList();

	/** 线程池 */
	ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 5, 2, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(5), new ThreadPoolExecutor.DiscardPolicy());

	/** 连接池全忙时，等待执行队列 */
	volatile private BlockingQueue<Worker> workqueue;

	public SshSessionMgr(ConnectConfig config) {
		jsch = new JSch();
		this.config = config;
		sessionPool = new ArrayList<SessionExt>(config.getMax());
		workqueue = new ArrayBlockingQueue<Worker>(WAIT_SIZE);
		this.biz = DEFAULT_NAME_PREFIX;
		init();
		runWorker();
	}

	public SshSessionMgr(ConnectConfig config, String biz) {
		jsch = new JSch();
		this.config = config;
		sessionPool = new ArrayList<SessionExt>(config.getMax());
		workqueue = new ArrayBlockingQueue<Worker>(WAIT_SIZE);
		this.biz = biz;
		init();
		runWorker();
	}

	/**
	 * 获取jsch
	 * 
	 * @return
	 */
	public JSch getJSch() {
		return jsch;
	}

	/**
	 * 获取连接配置
	 * 
	 * @return
	 */
	public ConnectConfig getConfig() {
		return config;
	}

	public void setBiz(String biz) {
		this.biz = biz;
	}

	public String getBiz() {
		return biz;
	}

	/**
	 * 初始化
	 */
	private void init() {
		synchronized (sessionPool) {
			int core = config.getCore();
			for (int i = 0; i < core; i++) {
				SessionExt session = openSession();
				if (null == session) {
					continue;
				}
				sessionPool.add(session);
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

	/**
	 * 回收session，值保留core个
	 */
	public void recycle() {
		synchronized (sessionPool) {
			for (int i = config.getCore(); i < config.getMax(); i++) {
				SessionExt session = sessionPool.get(i);
				if (!session.isIdle()) // 只回收空闲的
					continue;
				session.setState(SessionExt.STATE_DESTORING);
				session.recycle();
				session.setState(SessionExt.STATE_DESTORIED);
				// if (_TraceEnabled) {
				logger.info(
						Thread.currentThread().getName() + "正在回收ssh连接 " + session.toString(),
						"current sessionPool size:" + sessionPool.size() + " core:"
								+ config.getCore());
				// }
			}
		}
	}

	/**
	 * 打开一个会话，状态为空闲
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
				session.setTimeout(1000); // 默认1s
			} else {
				session.setTimeout(config.getTimeout());
			}
			session.connect(config.getTimeout());
			return new SessionExt(session, biz + (this.sessionPool.size() + 1));
		} catch (JSchException e) {
			if (null != session) {
				if (session.isConnected())
					session.disconnect();
				session = null;
			}
			logger.error("打开ssh会话失败," + config.toString(), e);
		}
		return null;
	}

	public void execute(Worker worker) throws RemoteException {
		if (null == worker)
			throw new RemoteException("任务为空");
		addWorker(worker);
	}

	public void addWorker(Worker worker) throws RemoteException {
		synchronized (workqueue) {
			try {
				while (workqueue.size() >= WAIT_SIZE) {
					workqueue.wait();
				}
				workqueue.put(worker);
				workqueue.notifyAll();
			} catch (InterruptedException e) {
				try {
					workqueue.put(worker);// 重试
					workqueue.notifyAll();
				} catch (InterruptedException e1) {
					logger.error("", e1);
				}
			}
		}
	}

	/**
	 * 轮询执行队列里的任务
	 */
	private void runWorker() {
		Thread t = new Thread() {
			public void run() {
				while (true) {
					synchronized (workqueue) {
						try {
							while (workqueue.size() == 0) {
								workqueue.wait();
							}
							final SessionExt session = getSession();
							final Worker work = workqueue.take();
							executor.execute(new Runnable() {
								@Override
								public void run() {
									runWorker(session, work);
								}
							});
							// 执行完毕，释放锁并唤醒其他线程
							workqueue.notifyAll();
						} catch (InterruptedException e) {
							logger.error("", e);
						}
					}
					// 退出同步块再执行

				}
			};
		};
		t.start();
	}

	/**
	 * 执行任务，执行完毕后将会话设置为空闲状态且将channel通道关闭
	 * 
	 * @param session
	 * @param worker
	 */
	private void runWorker(SessionExt session, Worker worker) {
		if (null == session || null == worker)
			return;
		try {
			worker.run(session);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			session.disconnectChannel();
			// 因为get的时候会锁，这里不需要锁
			session.setState(SessionExt.STATE_IDLE);
		}
	}

	/**
	 * 获取一个会话，并且将会话的状态改为运行中
	 * 
	 * @return
	 */
	public SessionExt getSession() {
		synchronized (sessionPool) {
			for (SessionExt session : sessionPool) {
				if (session.isIdle()) {
					// 有空闲的会话，直接使用
					session.setState(SessionExt.STATE_ACTIVE);
					return session;
				}
			}
			synchronized (sessionPool) {
				if (sessionPool.size() < config.getMax()) {
					// 会话创建数还没有达到最大值，创建
					SessionExt session = openSession();
					session.setState(SessionExt.STATE_ACTIVE);
					sessionPool.add(session);
					return session;
				}
			}
		}
		return null;
	}

	static public SshClient getClient(ConnectConfig config) {
		return new SshClient(config);
	}

	public String toString() {
		return config.toString();
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
		config.setTimeout(30000);
		config.setMax(10);
		config.setCore(2);
		config.setBasePath("/var/www/image/");
		final SshSessionMgr mgr = new SshSessionMgr(config);
		for (int i = 0; i < 50; i++) {
			final int j = i;
			Thread t = new Thread() {
				public void run() {
					try {
						mgr.execute(new Worker() {
							@Override
							public void run(SessionExt session) {
								try {
									InputStream stream = new FileInputStream(new File(
											"C:/Users/ajie/Desktop/arrow_top.png"));
									Channel channel = session.openChannel(3000, "sftp");
									ChannelSftp sftp = (ChannelSftp) channel;
									OutputStream out = sftp.put("/var/www/image/testimg" + (j + 1)
											+ ".png");
									byte[] buf = new byte[1024];
									int n = stream.read(buf);
									while (n != -1) {
										out.write(buf, 0, n);
										n = stream.read(buf);
									}
									out.flush();
									stream.close();
									out.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				};
			};
			t.start();
		}

	}
}
