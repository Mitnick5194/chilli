package com.ajie.chilli.support;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.thread.ThreadPool;

/**
 * 定时器（使用单位为毫秒ms）
 *
 * @author niezhenjie
 *
 */
public class TimingTask implements Runnable {
	protected static final Logger logger = LoggerFactory
			.getLogger(TimingTask.class);
	/** 首次执行的时间 */
	private long delay;
	/** 周期执行的间隔 单位值毫秒 0表示只执行一次 */
	private long interval;
	/** 定时执行的任务 */
	private Worker worker;
	/** 任务名 */
	private String name;

	/** 线程池 */
	private final ThreadPool pool;

	public static final TimingTask _Nil = new TimingTask();

	private TimingTask() {
		pool = null;
		delay = 0;
		interval = 0;
		worker = null;
	}

	/**
	 * 创建一个定时并周期性执行的定时器
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param delay
	 *            多少毫秒后开始第一次执行，单位ms
	 * @param interval
	 *            往后间隔多长时间执行，单位ms
	 */
	public TimingTask(ThreadPool pool, Worker worker, long delay, long interval) {
		this.pool = pool;
		this.worker = worker;
		this.delay = delay;
		this.interval = interval;
		start();
	}

	/**
	 * 延迟执行任务
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param firstExecute
	 *            执行时间
	 */
	public TimingTask(ThreadPool pool, Worker worker, long delay) {
		this(pool, worker, delay, 0);
	}

	private void start() {
		if (null == pool) {
			logger.warn(name + "任务未创建，启动失败，可忽略");
			return;
		}
		if (delay < 0) {
			return;
		}
		if (interval > 0) {
			pool.execute(this, delay, interval);
		} else {
			pool.execute(this, delay);
		}
		logger.info("启动定时任务" + name);
	}

	/**
	 * 取消任务
	 * 
	 * @return
	 */
	public void cancel() {
		if (null == pool) {
			logger.warn(name + "任务未创建，取消失败，可忽略");
			return;
		}
		Thread t = Thread.currentThread();
		if (!t.isInterrupted()) {
			t.interrupt();
			logger.info(name + "定时任务已取消");
		}
	}

	/**
	 * 创建一个延迟执行的任务
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param firstExecute
	 *            执行时间，日期格式
	 */
	public static TimingTask createTimingTask(ThreadPool pool, Worker worker,
			Date executeTime) {
		long delay = System.currentTimeMillis() - executeTime.getTime();
		return createTimingTask(pool, worker, delay);
	}

	/**
	 * 创建一个延迟执行的任务
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param firstExecute
	 *            执行时间，日期格式
	 */
	public static TimingTask createTimingTask(ThreadPool pool, String name,
			Worker worker, Date executeTime) {
		long delay = System.currentTimeMillis() - executeTime.getTime();
		TimingTask timing = createTimingTask(pool, worker, delay);
		timing.name = name;
		return timing;
	}

	/**
	 * 创建一个延迟执行的任务
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param firstExecute
	 *            执行时间，子串格式格式
	 */
	public static TimingTask createTimingTask(ThreadPool pool, Worker worker,
			long delay) {
		if (delay < 0) {
			// 已过时间，不执行
			return _Nil;
		}
		return createTimingTask(pool, worker, delay, 0);
	}

	/**
	 * 创建一个延迟执行的任务
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param firstExecute
	 *            执行时间，子串格式格式
	 */
	public static TimingTask createTimingTask(ThreadPool pool, String name,
			Worker worker, long delay) {
		if (delay < 0) {
			// 已过时间，不执行
			TimingTask timing = _Nil;
			timing.name = name;
			return timing;
		}
		TimingTask timing = createTimingTask(pool, worker, delay, 0);
		timing.name = name;
		return timing;
	}

	/**
	 * 定时任务
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param delay
	 *            多少毫秒后开始第一次执行
	 * @param interval
	 *            往后间隔多长时间执行
	 * @return
	 */
	public static TimingTask createTimingTask(ThreadPool pool, Worker worker,
			long delay, long interval) {
		return new TimingTask(pool, worker, delay, interval);
	}

	/**
	 * 定时任务
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param delay
	 *            多少毫秒后开始第一次执行
	 * @param interval
	 *            往后间隔多长时间执行
	 * @return
	 */
	public static TimingTask createTimingTask(ThreadPool pool, String name,
			Worker worker, long delay, long interval) {
		TimingTask timing = new TimingTask(pool, worker, delay, interval);
		timing.name = name;
		return timing;
	}

	/**
	 * 定时任务
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param firstExecuteDate
	 *            首次执行时间
	 * @param interval
	 *            往后间隔多长时间执行
	 * @return
	 */
	public static TimingTask createTimingTask(ThreadPool pool, Worker worker,
			Date firstExecuteTime, long interval) {
		long delay = calDelay(firstExecuteTime, interval);
		return new TimingTask(pool, worker, delay, interval);
	}

	/**
	 * 定时任务
	 * 
	 * @param pool
	 *            线程池
	 * @param worker
	 *            任务
	 * @param firstExecuteDate
	 *            首次执行时间
	 * @param interval
	 *            往后间隔多长时间执行
	 * @return
	 */
	public static TimingTask createTimingTask(ThreadPool pool, String name,
			Worker worker, Date firstExecuteTime, long interval) {
		long delay = calDelay(firstExecuteTime, interval);
		TimingTask timing = new TimingTask(pool, worker, delay, interval);
		timing.name = name;
		return timing;
	}

	/**
	 * 创建一个定时器
	 * 
	 * @param name
	 * @param worker
	 * @param firstExecuteDate
	 *            首次执行时间，格式 HH:mm:ss
	 * @param interVal
	 *            定时间隔
	 * @return
	 */
	public static TimingTask createTimingTask(ThreadPool pool, Worker worker,
			String firstExecuteDate, long interVal) {
		if (null == firstExecuteDate) {
			throw new NullPointerException("首次执行时间为空");
		}
		String[] split = firstExecuteDate.split(":");
		String sh = null, sm = null, ss = null;
		for (int i = 0; i < split.length; i++) {
			if (i == 0) {
				sh = split[i];
			} else if (i == 1) {
				sm = split[i];
			} else if (i == 2) {
				ss = split[i];
			}
		}
		int h, m, s;
		try {
			h = Integer.valueOf(sh);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("首次执行时间格式错误：" + firstExecuteDate);
		}
		if (h > 24) {
			throw new IllegalArgumentException("首次执行时间小时数大于24："
					+ firstExecuteDate);
		}
		try {
			m = Integer.valueOf(sm);
		} catch (Exception e) {
			m = 0;
		}
		if (m >= 60) {
			throw new IllegalArgumentException("首次执行时间分钟数大于或等于60："
					+ firstExecuteDate);
		}
		try {
			s = Integer.valueOf(ss);
		} catch (Exception e) {
			s = 0;
		}
		if (s > 60) {
			throw new IllegalArgumentException("首次执行时间秒数大于或等于60："
					+ firstExecuteDate);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		cal.set(Calendar.SECOND, s);
		return createTimingTask(pool, worker, cal.getTime(), interVal);
	}

	/**
	 * 创建一个定时器
	 * 
	 * @param name
	 * @param worker
	 * @param firstExecuteDate
	 *            首次执行时间，格式 HH:mm:ss
	 * @param interVal
	 *            定时间隔
	 * @return
	 */
	public static TimingTask createTimingTask(ThreadPool pool, String name,
			Worker worker, String firstExecuteTime, long interval) {
		TimingTask timing = createTimingTask(pool, worker, firstExecuteTime,
				interval);
		timing.name = name;
		return timing;
	}

	/**
	 * 根据时间，计算下次执行的时间
	 * 
	 * @param date
	 * @return
	 */
	static private long calDelay(Date firstExecuteTime, long interval) {
		long delay = System.currentTimeMillis() - firstExecuteTime.getTime();
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
		return delay;
	}

	@Override
	public void run() {
		try {
			worker.work();
		} catch (Exception e) {
			logger.error("定时任务执行失败", Thread.currentThread().getName(), e);
		}
	}
}
