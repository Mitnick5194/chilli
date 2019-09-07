package com.ajie.chilli.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 统一管理线程，不能执行定时任务
 *
 * @author niezhenjie
 * @Deprecated 请使用 ThreadPool
 */
@Deprecated
public class ThreadPoolManager {

	/** 最小线程数/核心数 */
	private int minSize;
	/** 最大线程数，超过则加入等待队列 */
	private int maxSize;
	/** 等待队列最大数 */
	private int queueSize;
	private String name;
	/** 线程池 */
	ThreadPoolExecutor executor;

	static final RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.AbortPolicy();

	static final String DEFAULT_NAME = "my-thread-pool-";

	/** 空闲程度 -- 绝对的空闲，正在执行数activeCount ==0 */
	public static final int IDLE_ABSOLUTE = 0x01;
	/** 空闲程度 -- 空闲 ，正在执行数activeCount < maxCount */
	public static final int IDLE_IDLE = 0x02;
	/** 空闲程度 -- 一般空闲，有点忙 ，等待队列任务数 <= queueSize-2 */
	public static final int IDLE_BUSINESS = 0x03;
	/** 空闲程度 -- 很忙，要爆了，队列只能再放两个任务了，等待队列任务数 >= queueSize-2 */
	public static final int IDLE_WARN = 0x04;

	public ThreadPoolManager(int minSize, int maxSize, int queueSize) {
		this(minSize, maxSize, queueSize, DEFAULT_NAME);
	}

	public ThreadPoolManager(int minSize, int maxSize, int queueSize,
			String name) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.queueSize = queueSize;
		this.name = name;
		init();
	}

	private void init() {
		executor = new ThreadPoolExecutor(minSize, maxSize, 2,
				TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueSize),
				new ThreadFactory() {

					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, getName());
						if (t.isDaemon()) {
							t.setDaemon(false);
						}
						return t;
					}
				}, HANDLER);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void execute(Runnable r) {
		if (null == r) {
			throw new NullPointerException();
		}
		executor.execute(r);
	}

	/**
	 * 当前线程池是否空闲
	 * 
	 * @param idleLevel
	 *            见：ThreadPoolManager.IDLE_XXX，如果不在常量范围的数，则以queue队列是否为空判定
	 * @return
	 */
	public boolean isIdle(int idleLevel) {
		int activeCount = getActiveCount();
		System.out.println(activeCount);
		int queueSize = executor.getQueue().size();
		int max = maxSize + queueSize;
		if (idleLevel == IDLE_WARN) {
			return queueSize - 2 >= this.queueSize;
		}
		if (idleLevel == IDLE_BUSINESS) {
			return activeCount < max - 2;
		}
		if (idleLevel == IDLE_IDLE) {
			return activeCount < maxSize;
		}
		if (idleLevel == IDLE_ABSOLUTE) {
			return activeCount == 0;
		}
		return isIdle();
	}

	/**
	 * 线程池是否为空闲，判断标准为等待队列任务数 ==0 (等待队列为空)
	 * 
	 * @return
	 */
	public boolean isIdle() {
		return executor.getQueue().size() == 0;
	}

	/**
	 * 当前活跃线程数，最大为maxCount
	 * 
	 * @return
	 */
	public int getActiveCount() {
		return executor.getActiveCount();
	}

	public static void main(String[] args) throws InterruptedException {
		ThreadPoolManager manager = new ThreadPoolManager(2, 10, 10);
		for (int i = 0; i < 30; i++) {
			try {
				manager.execute(new Runnable() {
					@Override
					public void run() {
						System.out.println(Thread.currentThread().getName());
					}
				});
			} catch (Exception e) {
				System.out.println("满了");
			}

		}
		Thread.sleep(10000);
		System.out.println(manager.isIdle(IDLE_ABSOLUTE));
	}
}
