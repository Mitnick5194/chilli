package com.ajie.chilli.cache;

import com.ajie.chilli.cache.simple.SimpleCacheMgr;

/**
 * 缓存管理中心，由spring容器管理，需要被管理的缓存需注入此管理中心，调用register()方法
 *
 * @author niezhenjie
 *
 */
public interface CacheMgr {

	/** 轻度报警默认阈值 单位MB */
	public static final int DEFAULT_MILDALERT_THRESHOLD = 1 << 8;
	/** 中度报警默认阈值 单位MB */
	public static final int DEFAULT_MODERATEALERT_THRESHOLD = 1 << 7;
	/** 严重报警默认阈值 单位MB */
	public static final int DEFAULT_SERIOUSALERT_THRESHOLD = 1 << 6;

	/** 正常状态 */
	public static final int STATE_NORMAL = 1 << 0;
	/** 长在回收 */
	public static final int STATE_CLEANING = 1 << 2;
	/** 停用部分缓存 */
	public static final int STATE_STOP_PART = 1 << 3;
	/** 停用全部缓存 */
	public static final int STATE_STOP_ALL = 1 << 4;

	/**
	 * 注册到管理中心
	 */
	void register(CacheListener cache);

	/**
	 * 获取缓存实体数量最大的缓存
	 * 
	 * @return
	 */
	CacheListener getMaxCache();

	/**
	 * 回收内存
	 */
	void recovery();

	/**
	 * 进行回收
	 * 
	 * @param cache
	 *            缓存监听器
	 */
	void doRecoveryPolicy(CacheListener cache);

	/**
	 * 停用所有缓存
	 * 
	 * @return
	 */
	boolean stopAllCache();

	/**
	 * 启用所有缓存
	 * 
	 * @return
	 */
	boolean startAllCache();

	/**
	 * 清空所有缓存
	 * 
	 * @return
	 */
	boolean cleanAllCache();

	/**
	 * 获取当前时间虚拟机剩余的可用内存
	 * 
	 * @return
	 */
	long getFreeMemory();

	/**
	 * 轻度报警，不用太紧张
	 * 
	 * @return
	 */
	/*
	boolean mildAlert();*/

	/**
	 * 设置轻度报警阈值
	 * 
	 * @param threshold
	 *            阈值 单位MB
	 */
	void setMildAlertThresHold(int threshold);

	/**
	 * 中度报警，要警惕啦
	 * 
	 * @return
	 */
	/*
	boolean moderateAlert();*/

	/**
	 * 设置中度报警阈值
	 * 
	 * @param threshold
	 *            阈值 单位MB
	 */
	void setModerateAlertThresHold(int threshold);

	/**
	 * 内存严重不足，赶紧清理所有内存，接近宕机啦
	 * 
	 * @return
	 */
	/*
	boolean seriousAlert();
	*/
	/**
	 * 设置严重报警阈值
	 * 
	 * @param threshold
	 *            阈值 单位MB
	 */
	void setSeriousAlertThresHold(int threshold);

	/**
	 * 当前缓存回收策略
	 * 
	 * @return
	 */
	AlertPolicy getAlertPolicy();

	/**
	 * 设置缓存回收策略
	 * 
	 * @param policy
	 *            {@link SimpleCacheMgr.POLICY_XXX.id}
	 * @return
	 */
	void setAlertPolicy(int policy);

	/**
	 * 获取当前内存空闲处于哪个策略
	 * 
	 * @return
	 */
	AlertPolicy getCurAlertPolicy();

	/**
	 * 报警策略
	 *
	 * @author niezhenjie
	 *
	 */
	interface AlertPolicy {
		/** 轻度策略回收项数 */
		public static final int MILD_SIZE = 1;
		/** 中度策略回收项数 */
		public static final int MODERATE_SIZE = 1 << 3;
		/** 重度策略回收项数 */
		public static final int SERIOUS_SIZE = 1 << 5;
		
		/**
		 * 名称
		 * 
		 * @return
		 */
		String getName();

		/**
		 * id
		 * 
		 * @return
		 */
		int getId();

		/**
		 * 一次回收缓存项个数
		 * 
		 * @return
		 */
		int getSize();

		void setSize(int size);

		/**
		 * 策略级别
		 * 
		 * @return
		 */
		int getLevel();

	}
}
