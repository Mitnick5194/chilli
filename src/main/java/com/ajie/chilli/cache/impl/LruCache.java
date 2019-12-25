package com.ajie.chilli.cache.impl;

import java.util.Iterator;
import java.util.Map;

import com.ajie.chilli.cache.Cache;
import com.ajie.chilli.utils.TimeUtil;

/**
 * 基于lru算法的双向链表缓存
 * 
 * @author niezhenjie
 */
public class LruCache<K, V> implements Cache<K, V> {
	/** 缓存默认大小 */
	public static final int DEFAULT_SIZE = 1 << 27;
	/** 缓存名字 */
	protected String name;
	/** 缓存大小 */
	protected long size;
	/** 项数 */
	protected long count;
	/** 缓存第一项指针 */
	private LruEntry<K, V> first;
	/** 缓存最后一项指针 */
	// private LruEntry<K, V> last;
	/** 过期时间 单位s */
	protected int timeout;
	/** 锁 */
	protected Object lock;

	public LruCache(Class<V> clazz) {
		name = clazz.getSimpleName();
		size = DEFAULT_SIZE;
		this.timeout = -1;// 不过期
	}

	public LruCache(Class<V> clazz, int size) {
		name = clazz.getSimpleName();
		this.size = size;
		this.timeout = -1;// 不过期
	}

	public LruCache(int timeout, Class<V> clazz) {
		this.timeout = timeout;
		name = clazz.getSimpleName();
		this.size = DEFAULT_SIZE;
	}

	public LruCache(Class<V> clazz, int size, int timeout) {
		name = clazz.getSimpleName();
		this.size = size;
		this.timeout = timeout;
	}

	@Override
	public V put(K key, V value) {
		assertSize();
		LruEntry<K, V> entry = findEntry(key);
		if (null != entry) {
			V old = entry.getValue();
			entry.setValue(value);
			return old;
		}
		LruEntry<K, V> newEntry = createEntry(key, value);
		putHead(newEntry);
		count++;
		return null;
	}

	/**
	 * 断言当前缓存项是否大于或等于size
	 */
	private void assertSize() {
		if (count >= size) {
			throw new IndexOutOfBoundsException(count + ">=" + size);
		}
	}

	/**
	 * 创建一个实体
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private LruEntry<K, V> createEntry(K key, V value) {
		LruEntry<K, V> entry = new LruEntry<K, V>(key, value);
		entry.setTimeout(timeout);
		return entry;
	}

	/**
	 * 将entry放入链表头部
	 * 
	 * @param entry
	 * @return
	 */
	private LruEntry<K, V> putHead(LruEntry<K, V> entry) {
		if (null == first) {// 当前链表为空
			first = entry;
			entry.hit();
			return entry;
		}
		// 将链表头的上一个指针指向entry
		first.setPre(entry);
		// 将entry的下一个指针指向表头（没有这一步，则entry的下一个指针仍然指向原来的，就会形成了两段链表）
		entry.setNext(first);
		// 表头变量指针指向entry（即让entry变成了第一项）
		first = entry;
		return entry;
	}

	/**
	 * 寻找实体（校验过期）
	 * 
	 * @param key
	 * @return
	 */
	private LruEntry<K, V> findEntry(Object key) {
		if (null == first) {
			return null;
		}
		if (first.getKey().equals(key)) {
			if (first.isTimeout()) {
				// 过期了
				return null;
			}
			return hitEntry(first);
		}
		LruEntry<K, V> next = first.getNext();
		while (null != next) {
			LruEntry<K, V> entry = next;
			if (entry.getKey().equals(key)) {
				if (entry.isTimeout()) {
					// 过期了
					return null;
				}
				return hitEntry(entry);
			}
			next = entry.next;
		}
		return null;
	}

	/**
	 * 命中实体并更新实体的访问时间戳，最后将实体放入头部(不校验过期)
	 * 
	 * @param entry
	 * @return
	 */
	private LruEntry<K, V> hitEntry(LruEntry<K, V> entry) {
		if (entry == first) {// 对比地址
			// 这么巧，就是第一项，不用移动位置了
			entry.hit();
			return entry;
		}
		// 放入头部
		return putHead(entry);
	}

	/**
	 * 将entry从链表中分离
	 * 
	 * @param entry
	 * @return
	 */
	private LruEntry<K, V> separateEntry(LruEntry<K, V> entry) {
		// entry的上一个指针
		LruEntry<K, V> pre = entry.getPre();
		// entry的下一个指针
		LruEntry<K, V> next = entry.getNext();
		// 将entry的上一个实体的下一个指针指向entry的下一个的实体,有点绕，就是
		// a->b>c>d变成了a>c>d，b脱离链表
		if (null != pre) {
			pre.setNext(next);
		}
		if (null != next) {
			next.setPre(pre);
		}
		return entry;
	}

	@Override
	public void putAll(Map<K, V> map) {
		if (null == map) {
			return;
		}
		Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<K, V> next = it.next();
			K key = next.getKey();
			V value = next.getValue();
			put(key, value);
		}
	}

	@Override
	public V remove(Object key) {
		LruEntry<K, V> entry = findEntry(key);
		if (null != entry) {
			entry = separateEntry(entry);
			V v = entry.getValue();
			entry = null; // let gc
			count--;
			return v;
		}
		return null;
	}

	@Override
	public V get(Object key) {
		LruEntry<K, V> entry = findEntry(key);
		if (null == entry) {
			return null;
		}
		return entry.getValue();
	}

	@Override
	public long size() {
		return count;
	}

	@Override
	public boolean isEmpty() {
		return count == 0;
	}

	@Override
	public void clear() {
		// 为了更好的gc，还是所有的实体都置为空吧（虽然直接将first置为空后，后面的实体都是不可达的gc也会被回收）
		if (null == first) {
			return;
		}
		LruEntry<K, V> entry = first;
		LruEntry<K, V> next = null;
		do {
			next = entry.getNext();
			separateEntry(entry);
		} while (null != next);
		count = 0;
	}

	static class LruEntry<K, V> {
		/** 键 */
		private K key;
		/** 值 */
		private V value;
		/** 上一个实体指针 */
		private LruEntry<K, V> next;
		/** 下一个实体指针 */
		private LruEntry<K, V> pre;
		/** 过期时间 */
		private int timeout;
		/** 最后命中时间戳 */
		private long lastHit;

		LruEntry(K k, V v) {
			key = k;
			value = v;
		}

		/**
		 * 是否过期
		 * 
		 * @return
		 */
		boolean isTimeout() {
			return System.currentTimeMillis() - lastHit > TimeUtil
					.secondToMills(timeout);
		}

		/**
		 * 命中
		 */
		void hit() {
			lastHit = System.currentTimeMillis();
		}

		K getKey() {
			return key;
		}

		V getValue() {
			return value;
		}

		void setValue(V v) {
			value = v;
		}

		void setNext(LruEntry<K, V> entry) {
			next = entry;
		}

		void setPre(LruEntry<K, V> entry) {
			pre = entry;
		}

		LruEntry<K, V> getNext() {
			return next;
		}

		LruEntry<K, V> getPre() {
			return pre;
		}

		void setTimeout(int timeout) {
			this.timeout = timeout;
		}
	}

	public static void main(String[] args) {
		LruCache<String, String> cache = new LruCache<>(1,String.class);
		cache.put("k1", "v1");
		cache.put("k2", "v2");
		cache.put("k3", "v3");
		cache.put("k2", "newValue");
		System.out.println(cache.get("k2"));
	}

}
