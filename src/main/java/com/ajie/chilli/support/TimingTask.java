package com.ajie.chilli.support;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 定时器
 *
 * @author niezhenjie
 *
 */
public class TimingTask implements Runnable {
	/** 首次执行的时间 */
	private Date firstExecute;
	/** 周期执行的间隔 单位值毫秒 0表示只执行一次 */
	private long interval;
	/** 定时器 */
	private ScheduledExecutorService service;
	/** 定时执行的任务 */
	private Worker worker;

	/**
	 * 创建一个定时并周期性执行的定时器
	 * 
	 * @param worker
	 * @param firstExecute
	 * @param interval
	 */
	public TimingTask(Worker worker, Date firstExecuteDate, long interVal) {
		this.worker = worker;
		this.firstExecute = firstExecuteDate;
		this.interval = interVal;
		service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("redid-timing-thread");
				return t;
			}
		});
		long delay = firstExecute.getTime() - new Date().getTime();
		if (0 == interval) { // 只执行一次
			if (delay < 0) { // 只运行一次，且开始运行的时间已经过去了，不再执行
				service = null;
			} else {
				service.schedule(this, delay, TimeUnit.MILLISECONDS);
			}
		} else { // 周期性执行
			if (delay >= 0) {
				service.scheduleAtFixedRate(this, delay, interVal, TimeUnit.MILLISECONDS);
			} else {
				// 首次开始的时间已经过去了，根据interval计算下次运行的时间吧
				delay = Math.abs(delay);
				// 计算过去了的时间是多少个周期，如3点运行，周期是4小时，现在是9点，则超过了一个周期，下次运行是第二个周期的时间：3+4+4
				int time = (int) (delay / interval);
				long next = ((time + 1) * interval) - delay;
				service.scheduleAtFixedRate(this, next, interval, TimeUnit.MILLISECONDS);
			}
		}
	}

	/**
	 * 创建一个定时只执行一次的定时器
	 * 
	 * @param worker
	 * @param firstExecute
	 */
	public TimingTask(Worker worker, Date firstExecute) {
		this(worker, firstExecute, 0);
	}

	@Override
	public void run() {
		worker.work();
	}
}
