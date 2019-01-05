package com.ajie.chilli.cache.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.cache.CacheListener;
import com.ajie.chilli.cache.CacheMgr;
import com.ajie.chilli.server.Server;
import com.ajie.chilli.server.simple.SimpleServer;

/**
 * 缓存管理中心
 *
 * @author niezhenjie
 *
 */
public class SimpleCacheMgr implements CacheMgr {
	private final static Logger logger = LoggerFactory.getLogger(SimpleCacheMgr.class);

	/** 内存过高执行策略，无需太紧张，默认回收策略 */
	public static final AlertPolicy POLICY_MILD_ALERT = new AlertPolicyItem("轻量回收策略", 0x1,
			AlertPolicy.MILD_SIZE, 0x100);
	/** 内存不足 */
	public static final AlertPolicy POLICY_MODERATE_ALERT = new AlertPolicyItem("一般回收策略", 0x2,
			AlertPolicy.MODERATE_SIZE, 0x200);
	/** 内存严重不足了，赶紧大量回收 */
	public static final AlertPolicy POLICY_SERIOUS_ALERT = new AlertPolicyItem("大量回收策略", 0x3,
			AlertPolicy.SERIOUS_SIZE, 0x300);
	/** 清空所有的缓存吧，不然随时可能宕机 */
	public static final AlertPolicy POLICY_EMPTY_CACHE = new AlertPolicyItem("清空缓存策略", 0x4,
			Integer.MAX_VALUE, 0x400);
	public static final AlertPolicy[] AlertPolicyItems = { POLICY_MILD_ALERT,
			POLICY_MODERATE_ALERT, POLICY_SERIOUS_ALERT, POLICY_EMPTY_CACHE };

	protected List<CacheListener> cacheListeners = Collections.emptyList();
	/** 轻度报警阈值 */
	protected int mildAlertThresHold;
	/** 中度报警阈值 */
	protected int moderateAlertThresHold;
	/** 严重报警阈值 */
	protected int seriousAlertThresHold;
	/** 服务器 */
	protected Server server;
	/** 缓存使用策略 */
	protected AlertPolicy policy;

	protected int state;

	public SimpleCacheMgr() {
		this(CacheMgr.DEFAULT_MILDALERT_THRESHOLD, CacheMgr.DEFAULT_MODERATEALERT_THRESHOLD,
				CacheMgr.DEFAULT_SERIOUSALERT_THRESHOLD);
	}

	public SimpleCacheMgr(int mildAlertThresHold, int moderateAlertThresHold,
			int seriousAlertThresHold) {
		this.mildAlertThresHold = mildAlertThresHold;
		this.moderateAlertThresHold = moderateAlertThresHold;
		this.seriousAlertThresHold = seriousAlertThresHold;
		cacheListeners = new ArrayList<>();
		server = new SimpleServer();
		policy = POLICY_MILD_ALERT;
		state = CacheMgr.STATE_NORMAL;
	}

	@Override
	synchronized public void register(CacheListener cache) {
		if (null == cache) {
			return;
		}
		cacheListeners.add(cache);
		logger.info("缓存已注册到管理中心:【name：" + cache.name() + "】");
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Server getServer() {
		return server;
	}

	@Override
	synchronized public CacheListener getMaxCache() {
		if (null == cacheListeners || cacheListeners.isEmpty()) {
			return null;
		}
		CacheListener max = null;
		int size = 0;
		for (CacheListener listener : cacheListeners) {
			if (listener.size() > size) {
				max = listener;
				size = listener.size();
			}
		}
		return max;
	}

	@Override
	public void recovery() {
		CacheListener maxCache = getMaxCache();
		if (null == maxCache) {
			return;
		}
		doRecoveryPolicy(maxCache);
	}

	@Override
	public void doRecoveryPolicy(CacheListener cache) {
		AlertPolicy curAlertPolicy = getCurAlertPolicy();
		if (null == curAlertPolicy)
			return;
		if (curAlertPolicy.getLevel() < policy.getLevel()) {
			return;
		}
		cache.recovery(curAlertPolicy.getSize());
		logger.info("执行回收，【" + cache.name() + "】，回收策略：" + curAlertPolicy.toString());
	}

	@Override
	public boolean stopAllCache() {
		if (cacheListeners.isEmpty())
			return false;
		boolean ret = true;
		for (CacheListener cache : cacheListeners) {
			if (cache.stopCache()) {
				logger.info("【" + cache.name() + "】已停用");
			} else {
				ret = false;
				logger.info("【" + cache.name() + "】停用失败");
			}
		}
		return ret;
	}

	@Override
	public boolean startAllCache() {
		if (cacheListeners.isEmpty())
			return false;
		boolean ret = true;
		for (CacheListener cache : cacheListeners) {
			if (cache.useCache()) {
				logger.info("【" + cache.name() + "】重新启用");
			} else {
				ret = false;
				logger.info("【" + cache.name() + "】启用失败");
			}
		}
		return ret;
	}

	@Override
	public boolean cleanAllCache() {
		if (cacheListeners.isEmpty())
			return false;
		boolean ret = true;
		for (CacheListener cache : cacheListeners) {
			cache.clear();
			logger.info("【" + cache.name() + "】已清空");
		}
		return ret;
	}

	@Override
	public AlertPolicy getCurAlertPolicy() {
		long freeMemory = getFreeMemory();
		return genAlertPolicyItem(freeMemory);
	}

	@Override
	public long getFreeMemory() {
		return server.getRealFreeMeory();
	}

	@Override
	public void setMildAlertThresHold(int threshold) {
		mildAlertThresHold = threshold;

	}

	@Override
	public void setModerateAlertThresHold(int threshold) {
		moderateAlertThresHold = threshold;

	}

	@Override
	public void setSeriousAlertThresHold(int threshold) {
		seriousAlertThresHold = threshold;

	}

	@Override
	public AlertPolicy getAlertPolicy() {
		return policy;
	}

	@Override
	public void setAlertPolicy(int policy) {
		AlertPolicy ap = AlertPolicyItem.getPolicyById(policy);
		this.policy = ap;
	}

	/**
	 * 根据传入的空闲内存返回对应的回收策略
	 * 
	 * @param memory
	 * @return
	 */
	protected AlertPolicy genAlertPolicyItem(long memory) {
		int memory_m = (int) memory / 1024 / 1024;
		if (memory_m <= 0)
			return POLICY_EMPTY_CACHE;
		if (memory_m <= seriousAlertThresHold)
			return POLICY_SERIOUS_ALERT;
		if (memory_m <= moderateAlertThresHold)
			return POLICY_MODERATE_ALERT;
		if (memory_m <= mildAlertThresHold)
			return POLICY_MILD_ALERT;
		return null;

	}

	static class AlertPolicyItem implements AlertPolicy {

		/** 策略名称 */
		private String name;
		/** 策略id */
		private int id;
		/** 回收缓存大小 */
		private int size;
		/** 级别，级别越高，使用权越优先 */
		private int level;

		/**
		 * 
		 * @param name
		 *            策略名称
		 * @param id
		 *            策略id
		 * @param size
		 *            策略每次回收缓存项数
		 * @param level
		 *            策略级别
		 */
		protected AlertPolicyItem(String name, int id, int size, int level) {
			this.name = name;
			this.id = id;
			this.size = size;
			this.level = level;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public int getSize() {
			return size;
		}

		@Override
		public void setSize(int size) {
			this.size = size;
		}

		@Override
		public int getLevel() {
			return level;
		}

		public static AlertPolicy getPolicyById(int id) {
			for (AlertPolicy policy : AlertPolicyItems) {
				if (id == policy.getId())
					return policy;
			}
			return null;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{name:").append(name).append(",");
			sb.append("id:").append(id).append(",");
			sb.append("size:").append(size).append(",");
			sb.append("level:").append(level).append("}");
			return sb.toString();
		}

	}

}
