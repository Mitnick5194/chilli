package com.ajie.utils.cache;

import java.util.Date;
import java.util.Map;

/**
 * 缓存基类
 * 
 * @author niezhenjie
 *
 * @param <K>
 * @param <V>
 */
public interface Cache<K, V> {

	/**
	 * 向缓存中添加一条数据，key已经存在，则更新key对应的值， 并返回旧的value，如果不存在，则返回null;
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	V put(K key, V value);

	/**
	 * 向缓存添加多条数据
	 * 
	 * @param map
	 */
	void putAll(Map<? extends K, ? extends V> map);

	/**
	 * 从缓存中移除一条数据，如果移除成功，则返回被移除的值，失败，返回null
	 * 
	 * @param key
	 * @return
	 */
	V remove(Object key);

	/**
	 * 查找key对应的值
	 * 
	 * @param key
	 * @return
	 */
	V get(Object key);

	/**
	 * 当前缓存的值的大小
	 * 
	 * @return
	 */
	int size();

	/**
	 * 缓存是否为空
	 * 
	 * @return
	 */
	boolean isEmpty();

	/**
	 * 最后的修改时间
	 * 
	 * @return
	 */
	Date lastModifyDate();

	/**
	 * 清空缓存
	 */
	void clear();
}
