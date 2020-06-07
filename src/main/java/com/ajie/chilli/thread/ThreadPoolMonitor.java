package com.ajie.chilli.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolMonitor {

	/** 监听的线程池集 */
	private List<ThreadPoolExecutor> pools;

	/** pools的锁 */
	private Object lock;

	public ThreadPoolMonitor() {
		pools = new ArrayList<ThreadPoolExecutor>();
	}

	public void register(ThreadPoolExecutor pool) {
		if (null == pool) {
			throw new NullPointerException("注册线程池为null");
		}
		synchronized (lock) {
			pools.add(pool);
			trim();
		}
	}

	public boolean unRegister(ThreadPoolExecutor pool) {
		if (null == pool) {
			return true;
		}
		synchronized (lock) {
			boolean b = pools.remove(pool);
			trim();
			return b;
		}
	}

	/**
	 * 锁内方法调用，无需加锁
	 */
	private void trim() {
		if (pools instanceof ArrayList) {
			ArrayList<ThreadPoolExecutor> list = (ArrayList<ThreadPoolExecutor>) pools;
			list.trimToSize();
			pools = list;
		}
	}

	@SuppressWarnings("unused")
	public static class PoolData {
		/** 线程池总个数 */
		private int allCount;
		/** 普通线程池个数 */
		private int poolCount;
		/** 定时线程池个数 */
		private int scheduleCount;
		/**线程总活跃数*/
		private int allActive;
		/**普通线程池的队列大小*/
		private int queueSize;
		

	
	}

}
