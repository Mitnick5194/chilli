package com.ajie.chilli.cache.impl;

import java.util.Map;

import com.ajie.chilli.cache.Cache;

public class ConcurrentMapCache<K,V> implements Cache<K, V> {
	/** 缓存默认大小 */
	public static final int DEFAULT_SIZE = 1 << 27;
	/** 缓存名字 */
	protected String name;
	/** 缓存大小 */
	protected long size;
	/** 项数 */
	protected long count;
	public ConcurrentMapCache(Class<V> clazz) {
		name = clazz.getSimpleName();
		size = DEFAULT_SIZE;
	}
	@Override
	public V put(K key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<K, V> map) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cleanAll() {
		// TODO Auto-generated method stub
		
	}

}
