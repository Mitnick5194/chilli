package com.ajie.chilli.cache.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.cache.Cache;
import com.ajie.chilli.cache.CacheListener;

/**
 * 以ConcurrentHashMap为存储介质的缓存，线程安全
 * 
 * @author niezhenjie
 *
 * @param <K>
 * @param <V>
 */
public class MapCache<K, V> implements Cache<K, V>, CacheListener {
	private final static Logger logger = LoggerFactory.getLogger(MapCache.class);
	/** 缓存最多项 */
	public static final int MAXIMUN_CAPACITY = (1 << 27) - 1;

	/** 初始化缓存默认值大小 */
	private static final int DEFAULT_SIZE = 10;

	protected Map<K, V> map;
	/** 最后修改时间 */
	protected Date lastModify;

	/** 缓存名字 */
	protected String name;

	/** 当前状态值 */
	protected int state;

	/**
	 * 无参构造方法，初始化一个默认大小的、线程安全的Map
	 */
	public MapCache(String name) {
		this(name, DEFAULT_SIZE);
	}

	public MapCache(String name, int size) {
		map = new ConcurrentHashMap<K, V>(size);
		this.name = name;
		state = SATAE_NORMAL;
	}

	public MapCache(String name, Map<? extends K, ? extends V> map) {
		map = new ConcurrentHashMap<K, V>(map);
		this.name = name;
		state = SATAE_NORMAL;
	}

	public MapCache(String name, Cache<? extends K, ? extends V> cache) {
		MapCache<? extends K, ? extends V> c;
		if (cache instanceof MapCache) {
			c = (MapCache<? extends K, ? extends V>) cache;
			map = new ConcurrentHashMap<K, V>(c.map);
		}
		this.name = name;
		state = SATAE_NORMAL;
	}

	public V put(K key, V value) {
		if (!checkState()) {
			return null;
		}
		checksize();
		V v = map.put(key, value);
		if (null == v || v != value) {
			updateModifyDate();
			logger.info(getName() + "添加一条数据{" + key + " : " + value + "}");
		}
		return v;
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		if (!checkState()) {
			return;
		}
		int size = map.size();
		int size2 = m.size();
		if (size2 > MAXIMUN_CAPACITY)
			throw new IndexOutOfBoundsException();
		if (MAXIMUN_CAPACITY - size < size2)
			throw new IndexOutOfBoundsException();
		updateModifyDate();
		map.putAll(m);
	}

	public V remove(Object key) {
		return map.remove(key);
	}

	public V get(Object key) {
		return map.get(key);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Date lastModifyDate() {
		return lastModify;
	}

	private void updateModifyDate() {
		lastModify = new Date();
	}

	public void clear() {
		map.clear();
	}

	private void checksize() {
		checksize(map.size());
	}

	private void checksize(int size) {
		if (size >= MAXIMUN_CAPACITY)
			throw new IndexOutOfBoundsException();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String name() {
		return getName();
	}

	@Override
	public boolean recovery(int size) {
		Set<Entry<K, V>> entrySet = map.entrySet();
		Iterator<Entry<K, V>> iterator = entrySet.iterator();
		boolean ret = false;
		while (iterator.hasNext() && size-- > 0) {
			iterator.next();
			iterator.remove();
			ret = true;
		}
		return ret;
	}

	@Override
	public boolean stopCache() {
		int state = this.state;
		if (state == SATAE_STOP)
			return true;
		state = SATAE_STOP;
		this.state = state;
		return true;

	}

	@Override
	public boolean useCache() {
		int state = this.state;
		if (state == SATAE_NORMAL)
			return true;
		state = SATAE_NORMAL;
		this.state = state;
		return true;
	}

	private boolean checkState() {
		if (state == SATAE_STOP)
			return false;
		return true;
	}

}
