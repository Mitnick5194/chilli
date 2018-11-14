package com.ajie.chilli.cache;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.cache.impl.MapCache;

/**
 * 缓存工厂
 *
 * @author niezhenjie
 *
 */
public final class CacheFactory {
	private final static Logger logger = LoggerFactory.getLogger(CacheFactory.class);

	/** 集中管理所有的缓存，key为缓存名，可以是简单类名或全限命名 */
	public static final Map<String, Cache<String, ?>> caches = new HashMap<String, Cache<String, ?>>();

	private CacheFactory() {

	}

	/**
	 * 打开一个缓存并注册到管理中心，如果已经存在了，返回，不存在，创建
	 * 
	 * @param clazz
	 * @param isQualified
	 *            是否使用全限命名，false则使用simpleName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	synchronized public static <V> Cache<String, V> openCache(Class<V> clazz, boolean isQualified,
			CacheMgr cacheMgr) {
		String name;
		if (isQualified) {
			name = clazz.getName();
		} else {
			name = clazz.getSimpleName();
		}
		Cache<String, ?> cache = caches.get(name);
		if (null != cache) {
			return (Cache<String, V>) cache;
		}
		cache = new MapCache<String, V>(name);
		logger.info("创建缓存：【name:" + name + "】");
		if (null != cacheMgr && cache instanceof CacheListener) {
			CacheListener cl = (CacheListener) cache;
			cacheMgr.register(cl);
		}
		return (Cache<String, V>) cache;
	}

	/**
	 * 打开一个缓存，不注册到管理中心，如果已经存在了，返回，不存在，创建
	 * 
	 * @param clazz
	 * @param isQualified
	 *            是否使用全限命名，false则使用simpleName
	 * @return
	 */
	synchronized public static <V> Cache<String, V> openCache(Class<V> clazz, boolean isQualified) {
		return openCache(clazz, isQualified, null);
	}

	/**
	 * 打开一个缓存，不注册到管理中心，如果已经存在了，返回，不存在，创建
	 * 
	 * @param clazz
	 * @return
	 */
	synchronized public static <V> Cache<String, V> openCache(Class<V> clazz) {
		return openCache(clazz, false, null);
	}

	public static void main(String[] args) {
		Cache<String, String> cache1 = CacheFactory.openCache(String.class);
		cache1.put("k1", "v1");
		cache1.put("k2", "v2");

		Cache<String, Integer> cache2 = CacheFactory.openCache(Integer.class);
		cache2.put("IK1", 1);
		cache2.put("IK2", 2);

		System.out.println(cache1.get("k1"));
		System.out.println(cache2.get("IK2"));
	}

}
