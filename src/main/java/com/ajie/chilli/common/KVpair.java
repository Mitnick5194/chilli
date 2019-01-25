package com.ajie.chilli.common;

/**
 * 简单的key value id对照
 *
 * @author niezhenjie
 *
 */
public class KVpair {

	/** 项名 */
	private String name;

	/** 项值 */
	private Object value;
	/** 项id */
	private int id;

	private KVpair(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	private KVpair(String name, Object value, int id) {
		this.name = name;
		this.value = value;
		this.id = id;
	}

	private KVpair(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setValue(Object obj) {
		this.value = obj;
	}

	public Object getValue() {
		return this.value;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static KVpair valueOf(String name, Object value) {
		KVpair kv = new KVpair(name, value);
		return kv;
	}

	public static KVpair valueOf(String name, int id) {
		KVpair kv = new KVpair(name, id);
		return kv;
	}

	public static KVpair valueOf(String name, String value, int id) {
		KVpair kv = new KVpair(name, value, id);
		return kv;
	}
}
