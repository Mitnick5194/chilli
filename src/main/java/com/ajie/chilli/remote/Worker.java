package com.ajie.chilli.remote;

/**
 * ssh执行任务
 *
 * @author niezhenjie
 *
 */
public interface Worker {

	void run(SessionExt session);
}
