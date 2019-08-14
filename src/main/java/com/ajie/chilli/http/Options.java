package com.ajie.chilli.http;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 链接信息<br>
 * 
 * @author ajie
 *
 */
public class Options {

	/** 链接 */
	private String url;

	/** 原始链接数据 */
	private String originData;

	/**
	 * 请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，<br>
	 * 多少时间内无法返回数据，就直接放弃此次调用。 -1表示不设置
	 * */
	private int readTimeout;

	/** 设置连接超时时间，单位毫秒 -1表示不设置 */
	private int connectTimeout;

	/** 权重 0-10 */
	private int weight;

	/** 是否活跃，false则不调用该链接 */
	private boolean isActive;

	/** 调用次数 */
	volatile private AtomicInteger couter = new AtomicInteger();

	/** 权重范围 */
	private WeightRang weightRang;

	public Options(String url) {
		this(url, HttpInvoke.DEFALUT_READ_TIMEOUT,
				HttpInvoke.DEFALUT_CONNECT_TIMEOUT);
	}

	public Options(String url, int socketTimeout, int connectTimeout) {
		this(url, socketTimeout, connectTimeout, 1, true);
	}

	public Options(String url, int readTimeout, int connectTimeout,
			int weight, boolean isActive) {
		this.url = url;
		this.readTimeout = readTimeout;
		this.connectTimeout = connectTimeout;
		this.weight = weight;
		this.isActive = isActive;
	}

	public String getUrl() {
		return url;
	}

	public int getWeight() {
		return weight;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	/**
	 * 当前链接是否可用
	 * 
	 * @return
	 */
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * 计数器+1
	 * 
	 * @return +1后的值
	 */
	public int addCount() {
		return couter.addAndGet(1);
	}

	/**
	 * 返回当前计数器的值
	 * 
	 * @return
	 */
	public int getCount() {
		return couter.get();
	}

	public WeightRang getWeightRang() {
		return weightRang;
	}

	public void setWeightRang(WeightRang rang) {
		weightRang = rang;
	}

	public String toString() {
		return originData;
	}

	/**
	 * 权重范围
	 * 
	 * @author niezhenjie
	 *
	 */
	public static class WeightRang {
		private int start;
		private int end;

		public static WeightRang valueOf(int start, int end) {
			WeightRang wr = new WeightRang();
			wr.start = start;
			wr.end = end;
			return wr;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		/**
		 * pointer是否在范围内
		 * 
		 * @param pointer
		 * @return
		 */
		public boolean isHit(int pointer) {
			return pointer >= start && pointer <= end;
		}

		public String toString() {
			return start + "~" + end;
		}
	}

	/**
	 * Options构建者
	 * 
	 * @author niezhenjie
	 *
	 */
	public static class Builder {
		private Options wrap;

		private Builder(String url) {
			wrap = new Options(url);
		}

		public static Builder getBuilder(String url) {
			Builder build = new Builder(url);
			return build;
		}

		public Builder setSocketTimeout(int timeout) {
			wrap.readTimeout = timeout;
			return this;
		}

		public Builder setConnectTimeout(int timeout) {
			wrap.connectTimeout = timeout;
			return this;
		}

		public Builder setWeight(int weight) {
			wrap.weight = weight;
			return this;
		}

		public Builder setWeightRang(WeightRang rang) {
			wrap.weightRang = rang;
			return this;
		}

		public Builder setActive(boolean isActive) {
			wrap.isActive = isActive;
			return this;
		}

		public Builder setOriginData(String originData) {
			wrap.originData = originData;
			return this;
		}

		public Options build() {
			return wrap;
		}
	}
}
