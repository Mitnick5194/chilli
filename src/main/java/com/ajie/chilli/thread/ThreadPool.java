package com.ajie.chilli.thread;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池，普通线程和定时线程均可用，建议系统全局且单例
 * <p>
 * 定时任务任务队里无限大，所以使用两个线程池，一个是普通的线程池，一个是定时任务的线程池<br>
 * 默认不开启定时任务线程池，调用isOpenSchdule(true)可开启
 * <p>
 * 特别说明：定时任务的单位统一使用毫秒ms
 * 
 * @author niezhenjie
 */
public class ThreadPool {
	/** 线程默认名字 */
	public static final String DEFAULT_NAME = "bg-thread-";
	/** 定时线程池线程名字 */
	public static final String DEFAULT_SCHDULE_NAME = "bg-schdule-thread-";

	/** 默认队列大小 */
	static final int DEFAULT_QUEUE_SIZE = 10;

	/** 开启定时任务线程池 */
	static final int OPTION_OPEN_SCHDULE = 1;

	/** 拒绝操作 */
	static final RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.AbortPolicy();

	/** 最小线程数/核心数 */
	private int minPoolSize;
	/** 最大线程数，超过则加入等待队列 */
	private int maxPoolSize;
	/** 普通线程池线程名 */
	private String poolName;
	/** 线程池任务队里大小 */
	private int queueSize;
	/** 定时线程池最小线程数 */
	private int scheduleMinSize;
	/** 定时任务线程池最大线程数 */
	private int scheduleMaxSize;
	/** 定时任务线程名 */
	private String schedulePoolName;
	/** 线程池 */
	ThreadPoolExecutor threadPool;
	/** 定时任务线程池 */
	ScheduledThreadPoolExecutor schedulePool;

	private int options;

	public ThreadPool() {
		this(DEFAULT_NAME);
	}

	public ThreadPool(String name) {
		this(name, 0, Integer.MAX_VALUE);
	}

	public ThreadPool(String name, int minSize, int maxSize) {
		this(name, minSize, maxSize, 0);
	}

	public ThreadPool(String poolName, int minPoolSize, int maxPoolize,
			int queueSize) {
		this.poolName = poolName;
		this.minPoolSize = minPoolSize;
		this.maxPoolSize = maxPoolize;
		if (queueSize == 0) {
			queueSize = DEFAULT_QUEUE_SIZE;
		}
		this.queueSize = queueSize;
		init();
	}

	private void init() {
		threadPool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, 2L,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize),
				new ThreadFactory() {
					String name = ThreadPool.this.poolName;

					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						if (null != name) {
							t.setName(name + (threadPool.getTaskCount() + 1));
						}
						return t;
					}
				}, HANDLER);
	}

	private void initSchdule() {
		schedulePool = new ScheduledThreadPoolExecutor(scheduleMinSize,
				new ThreadFactory() {
					String name = ThreadPool.this.schedulePoolName;

					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						if (null != name) {
							t.setName(name + (schedulePool.getTaskCount() + 1));
						}
						return t;
					}
				}, HANDLER);
		if (scheduleMaxSize > 0) {
			schedulePool.setMaximumPoolSize(scheduleMaxSize);
		}
		schedulePool.setKeepAliveTime(2L, TimeUnit.SECONDS);
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public int getScheduleMinSize() {
		return scheduleMinSize;
	}

	public void setScheduleMinSize(int scheduleMinSize) {
		this.scheduleMinSize = scheduleMinSize;
	}

	public int getMaxScheduleMaxSize() {
		return scheduleMaxSize;
	}

	public void setScheduleMaxSize(int scheduleMaxSize) {
		this.scheduleMaxSize = scheduleMaxSize;
	}

	public String getSchedulePoolName() {
		return schedulePoolName;
	}

	public void setSchedulePoolName(String schedulePoolName) {
		this.schedulePoolName = schedulePoolName;
	}

	public void setOpenSchdule(boolean b) {
		if (b) {
			setOptions(OPTION_OPEN_SCHDULE);
			initSchdule();
		} else {
			schedulePool.shutdown();
			setOptions(-OPTION_OPEN_SCHDULE);
		}
	}

	/**
	 * 这只选项，负数表示清除标志位
	 * 
	 * @param option
	 */
	private void setOptions(int option) {
		if (option > 0) {
			options |= option;
		} else {
			option = (-option);
			options &= ~option;
		}
	}

	/**
	 * 是否为指定选项
	 * 
	 * @param option
	 * @return
	 */
	private boolean isOption(int option) {
		return option == (options & option);
	}

	/**
	 * 执行一个任务
	 * 
	 * @param command
	 *            任务
	 */
	public void execute(Runnable command) {
		threadPool.execute(command);
	}

	/**
	 * 延迟执行一个任务
	 * 
	 * @param command
	 *            任务
	 * @param delay
	 *            延迟时间
	 * @param unit
	 *            时间单位
	 */
	public void execute(Runnable command, long delay) {
		assertOpenSchedulePool();
		schedulePool.schedule(command, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * 定时执行一个任务
	 * 
	 * @param command
	 *            任务
	 * @param delay
	 *            延迟多久执行第一次
	 * @param interval
	 *            往后执行的间隔
	 * @param unit
	 *            单位，delay和interval共用
	 */
	public void execute(Runnable command, long delay, long interval) {
		assertOpenSchedulePool();
		schedulePool.scheduleWithFixedDelay(command, delay, interval,
				TimeUnit.MILLISECONDS);
	}

	/**
	 * 指定时间执行，且只会执行一次
	 * 
	 * @param command
	 * @param executeTime
	 */
	public void execute(Runnable command, Date executeTime) {
		assertOpenSchedulePool();
		long delay = System.currentTimeMillis() - executeTime.getTime();
		if (delay > 0) {
			return;// 时间过了
		}
		execute(command, delay);
	}

	/**
	 * 定时执行一个任务
	 * 
	 * @param command
	 *            任务
	 * @param firstExecuteTime
	 *            是什么时候执行第一次
	 * @param interval
	 *            往后执行的间隔
	 * @param unit
	 *            单位，delay和interval共用
	 */
	public void execute(Runnable command, Date firstExecuteTime, long interval) {
		assertOpenSchedulePool();
		long delay = System.currentTimeMillis() - firstExecuteTime.getTime();
		if (0 == interval) { // 只执行一次
			execute(command, firstExecuteTime);
		} else { // 周期性执行
			if (delay >= 0) {
				// 首次开始的时间已经过去了，根据interval计算下次运行的时间吧
				delay = Math.abs(delay);
				// 计算过去了的时间是多少个周期，如3点运行，周期是4小时，现在是9点，则超过了一个周期，下次运行是第二个周期的时间：3+4+4
				int time = (int) (delay / interval);
				long next = ((time + 1) * interval) - delay;
				if (0 == next) {
					// 这么巧，刚好卡正运行的点
					next = 0;
				}
				delay = next;
			}
			execute(command, delay, interval);
		}
	}

	private void assertOpenSchedulePool() {
		if (!isOption(OPTION_OPEN_SCHDULE)) {
			throw new IllegalArgumentException("定时任务未开启");
		}
	}

	public static void main(String[] args) {
		ThreadPool pool = new ThreadPool("test-thread-", 1, 1);
		pool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("1开始:"
							+ Thread.currentThread().getName());
					Thread.sleep(10000);
					System.out.println("1结束");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		pool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					System.out
							.println("2开始" + Thread.currentThread().getName());
					Thread.sleep(1000);
					System.out.println("2结束");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		pool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					System.out
							.println("3开始" + Thread.currentThread().getName());
					Thread.sleep(10000);
					System.out.println("3结束");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		pool.setScheduleMaxSize(2);
		pool.setOpenSchdule(true);
		pool.execute(new Runnable() {

			@Override
			public void run() {
				System.out.println("执行定时任务1："
						+ Thread.currentThread().getName());
			}
		}, 5000);
		pool.execute(new Runnable() {

			@Override
			public void run() {
				System.out.println("执行定时任务2");
			}
		}, 5000);
		pool.execute(new Runnable() {

			@Override
			public void run() {
				System.out.println("执行定时任务3");
			}
		}, 5000);
		pool.execute(new Runnable() {

			@Override
			public void run() {
				System.out.println("执行定时任务4");
			}
		}, 5000);
	}
}
