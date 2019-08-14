package com.ajie.chilli.http;

import java.net.URL;
import java.net.URLConnection;

/**
 * 网络连通性测试
 * 
 * @author niezhenjie
 *
 */
public class Ping {

	public static PingResult ping(String url) {
		PingResult result = new PingResult(url);
		try {
			long start = System.currentTimeMillis();
			URL u = new URL(url);
			URLConnection connect = u.openConnection();
			connect.connect();
			long end = System.currentTimeMillis();
			result.setExpire(end - start);
			result.setPingAble(true);
		} catch (Exception e) {
			result.setPingAble(false);
			result.setThrowable(e);
		}
		return result;
	}

	public static void main(String[] args) {
		PingResult result = ping("http://www.baidu1.com");
		System.out.println(result);
	}

	public static class PingResult {
		private String url;
		/** 耗时 单位ms */
		private long expire;
		/** 是否可ping通 */
		public boolean pingAble;
		/** 异常 */
		private Throwable e;

		public PingResult(String url) {
			this.url = url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUrl() {
			return url;
		}

		public void setExpire(long expire) {
			this.expire = expire;
		}

		public long getExpire() {
			return expire;
		}

		public void setPingAble(boolean able) {
			pingAble = able;
		}

		public boolean isPingAble() {
			return pingAble;
		}

		public void setThrowable(Throwable e) {
			this.e = e;
		}

		public Throwable getThrowable() {
			return e;
		}

		public String toString() {
			if (pingAble) {
				return "{url:" + url + ",pingAble:" + true + ",expire:"
						+ expire + "}";
			} else {
				return e.toString();
			}
		}
	}
}
