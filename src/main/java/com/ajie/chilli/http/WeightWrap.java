package com.ajie.chilli.http;

/**
 * 权重
 * 
 * @author ajie
 *
 */
public class WeightWrap {
	/** 链接 */
	private String url;
	/** 权重 */
	private int weight;
	/** 权重列表范围开始 */
	private int start;
	/** 权重列表范围结束 */
	private int end;

	public String getUrl() {
		return url;
	}

	public int getWeight() {
		return weight;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public static WeightWrap valueOf(String url, int weight) {
		WeightWrap wrap = new WeightWrap();
		wrap.url = url;
		wrap.weight = weight;
		return wrap;
	}

	/**
	 * 给定的下标是否在范围内
	 * 
	 * @param pointer
	 * @return
	 */
	public boolean isHit(int pointer) {
		return pointer >= start && pointer <= end;
	}
}
