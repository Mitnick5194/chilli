package com.ajie.chilli.support;

/**
 * 定时器定时执行的任务
 *
 * @author niezhenjie
 *
 */
public interface Worker {
	/** 执行任务 */
	void work() throws Exception;
}
