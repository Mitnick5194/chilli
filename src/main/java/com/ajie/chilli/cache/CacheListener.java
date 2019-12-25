package com.ajie.chilli.cache;

/**
 * 缓存监听器，供缓存管理中心管理，当内存紧张时，触发缓存回收（被管理的缓存需实现该接口）
 *
 * @author niezhenjie
 *
 */
public interface CacheListener {

	/**
	 * 缓存器名称
	 * 
	 * @return
	 */
	String name();

	/**
	 * 缓存大小
	 * 
	 * @return
	 */
	long size();

	/**
	 * 内存回收
	 * 
	 * @param size
	 *            回收大小,默认为1
	 * @return
	 */
	boolean recovery(int size);

	/**
	 * 清空该缓存
	 * 
	 * @return
	 */
	void clear();

	/**
	 * 停止使用该缓存
	 * 
	 * @return
	 */
	boolean stopCache();

	/**
	 * 启用该缓存
	 * 
	 * @return
	 */
	boolean useCache();

	

}
