package com.ajie.chilli.cache.impl;

import java.util.Iterator;
import java.util.Map;

import com.ajie.chilli.cache.Cache;
import com.ajie.chilli.utils.TimeUtil;
import com.ajie.chilli.utils.Toolkits;

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
	private LruEntry<K, V> last;
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
			hitEntry(entry);
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
			last = entry;// 只有一项，第一个也是最后一个
			entry.hit();
			return entry;
		}
		// 将链表头的上一个指针指向entry
		first.setPre(entry);
		// 将entry的下一个指针指向表头（没有这一步，则entry的下一个指针仍然指向原来的，就会形成了两段链表）
		entry.setNext(first);
		// 表头变量指针指向entry（即让entry变成了第一项）
		first = entry;
		entry.hit();
		return entry;
	}

	/**
	 * 寻找实体（不校验过期）
	 * 
	 * @param key
	 * @return
	 */
	private LruEntry<K, V> findEntry(Object key) {
		if (null == first) {
			return null;
		}
		if (first.getKey().equals(key)) {
			return first;
		}
		LruEntry<K, V> next = first.getNext();
		while (null != next) {
			LruEntry<K, V> entry = next;
			if (entry.getKey().equals(key)) {
				return entry;
			}
			next = entry.next;
		}
		return null;
	}

	/**
	 * 命中实体并更新实体的访问时间戳，最后将实体从链表中分离然后放入头部(不校验过期)
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
		if (entry == last) {
			// 这是最后一项，改变一下last的指针
			last = entry.getPre();
		}
		// 将entry从链表中分离
		separateEntry(entry);
		// 放入头部(里面会更新时间戳)
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
		// 将entry的前后指针置空
		entry.pre = null;
		entry.next = null;
		// 如果最后一项，则将last指针指向pre
		if (entry == last) {
			last = pre;
		}
		// 如果是第一项，则将first指针指向next
		if (entry == first) {
			first = next;
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
			separateEntry(entry);
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
		if (null == entry || entry.isTimeout()) {
			return null;
		}
		return hitEntry(entry).getValue();
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
	public void cleanAll() {
		// 为了更好的gc，还是所有的实体都置为空吧（虽然直接将first置为空后，后面的实体都是不可达的gc也会被回收）
		if (null == first) {
			return;
		}
		while (null != first) {
			remove(first.getKey());// 分离方法会处理first的指针
		}
		count = 0;
	}

	/**
	 * 清除缓存过期项
	 */
	public void cleanup() {
		// 从链表尾部遍历
		if (!last.isTimeout()) {
			return; // 最后一项都没过期，就没有过期的了
		}
		while (null != last && last.isTimeout()) {
			remove(last.getKey());
		}
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
		/** 命中次数 */
		private long hitTimes;

		LruEntry(K k, V v) {
			key = k;
			value = v;
			lastHit = System.currentTimeMillis();
		}

		/**
		 * 是否过期
		 * 
		 * @return
		 */
		boolean isTimeout() {
			if (timeout == -1) {
				return false;
			}
			return System.currentTimeMillis() - lastHit > TimeUtil
					.secondToMills(timeout);
		}

		/**
		 * 命中
		 */
		void hit() {
			lastHit = System.currentTimeMillis();
			hitTimes++;
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

		boolean hasNext() {
			return null != next;
		}

		boolean hasPre() {
			return null != pre;
		}

		long getHitTimes() {
			return hitTimes;
		}

		public String toString() {
			return key + "=" + key + ",value=" + value + ",timeout=" + timeout
					+ ";hitTimes:" + hitTimes;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		final LruCache<String, User> cache = new LruCache<>(User.class);
		final String key = Toolkits.genRandomStr(16);
		User u = new User("ajie");
		cache.put(key, u);
		Thread.sleep(1000);
		new Thread(new Runnable() {

			@Override
			public void run() {
				User user = cache.get(key);
				System.out.println(user.getName());
			}
		}).start();

		/*
		 * for (int i = 0; i < 200000; i++) { cache.put(i, i); }
		 * System.out.println("add complte"); long start =
		 * System.currentTimeMillis(); System.out.println(cache.get(1)); long
		 * end = System.currentTimeMillis(); System.out.println("耗时：" + (end -
		 * start) + "ms"); start = System.currentTimeMillis();
		 * System.out.println(cache.get(1)); end = System.currentTimeMillis();
		 * System.out.println("耗时：" + (end - start) + "ms");
		 */

	}

	static class User {
		private String name;

		public User(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
