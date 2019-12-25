package com.ajie.chilli.cache;

import java.util.HashMap;

/**
 * 基于lru算法的缓存
 * 
 * @author niezhenjie
 *
 */
public class LruCache<K, V> implements CacheListener {

	/** 默认缓存最大项数 */
	public static final int DEFALUT_COUTER = Integer.MAX_VALUE - 1;

	public static final int STATE_NORMAL = 1 << 0;

	/** 缓存名字 */
	protected String name;

	/** 缓存项 */
	protected int counter;

	/** 标记 */
	protected int mark;

	protected int state;

	protected Entry<K, V> firstEntry;

	protected Entry<K, V> lastEntry;

	public LruCache(Class<?> clazz) {
		this.name = clazz.getSimpleName();
		this.counter = DEFALUT_COUTER;
	}

	public LruCache(Class<?> clazz, int count) {
		this(clazz);
	}

	public void mark(int mark) {
		if (mark < 0) {
			mark = -mark;
			this.mark &= ~mark;
		}
		this.mark |= mark;
	}

	public boolean isMark(int mark) {
		return (this.mark & mark) == mark;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean recovery(int size) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean stopCache() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean useCache() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 实体
	 * 
	 * @author niezhenjie
	 *
	 * @param <K>
	 * @param <V>
	 */
	protected static class Entry<K, V> {
		/** 键 */
		protected K key;
		/** 值 */
		protected V value;
		/** 最后更新时间戳 */
		protected long lastUpdateTimestamp;
		/** 上一个实体 */
		protected Entry<K, V> before;
		/** 下一个实体 */
		protected Entry<K, V> after;

		protected Entry(K k) {
			this.key = k;
			this.value = null;
			this.lastUpdateTimestamp = System.currentTimeMillis();
			this.before = this;
			this.after = this;
		}

		protected Entry(Entry<K, V> entry) {
			key = entry.key;
			value = entry.value;
			lastUpdateTimestamp = entry.lastUpdateTimestamp;
			before = entry.before;
			after = entry.after;
		}

		public Entry<K, V> getBefore() {
			return before;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public Entry<K, V> getAfter() {
			return after;
		}

	}

	/**
	 * 迭代器接口
	 * 
	 * @author niezhenjie
	 *
	 * @param <K>
	 */
	public static interface Iterator<K> {
		boolean hasNext();

		<V> Entry<K, V> next();
	}

	public static void main(String[] args) {
		HashMap<String, String> map = new HashMap<>();
		map.put("name", "ajie");
		map.put("city", "zq");
		java.util.Iterator<java.util.Map.Entry<String, String>> it = map
				.entrySet().iterator();
		while (it.hasNext()) {
			java.util.Map.Entry<String, String> next = it.next();
			System.out.println(next.getValue());
		}
	}
}
