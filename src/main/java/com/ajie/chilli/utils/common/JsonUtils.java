package com.ajie.chilli.utils.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONLibDataFormatSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * fastjson封装的工具类<Br>
 * 转换成Json String后，可以直接被js识别为json，并不需要在Java中转换成JsonObject<Br>
 * 也不需要在js中使用JSON.parse或eval
 * 
 * @author niezhenjie
 */
public class JsonUtils {

	private static final SerializeConfig config;

	static {
		config = new SerializeConfig();
		config.put(java.util.Date.class, new JSONLibDataFormatSerializer()); // 使用和json-lib兼容的日期输出格式
		config.put(java.sql.Date.class, new JSONLibDataFormatSerializer()); // 使用和json-lib兼容的日期输出格式
	}

	private static final SerializerFeature[] features = { SerializerFeature.WriteMapNullValue, // 输出空置字段
			SerializerFeature.WriteNullListAsEmpty, // list字段如果为null，输出为[]，而不是null
			SerializerFeature.WriteNullNumberAsZero, // 数值字段如果为null，输出为0，而不是null
			SerializerFeature.WriteNullBooleanAsFalse, // Boolean字段如果为null，输出为false，而不是null
			SerializerFeature.WriteNullStringAsEmpty // 字符类型字段如果为null，输出为""，而不是null
	};

	/**
	 * 
	 * @param object
	 * @return
	 */
	public static String toJSONString(Object object) {
		return JSON.toJSONString(object, config, features);
	}

	public static String toJSONNoFeatures(Object object) {
		return JSON.toJSONString(object, config);
	}

	public static Object toBean(String text) {
		return JSON.parse(text);
	}

	/** 将字串转换成bean，也可以转换成JsonObject */
	public static <T> T toBean(String text, Class<T> clazz) {
		return JSON.parseObject(text, clazz);
	}

	// 转换为数组
	public static <T> Object[] toArray(String text) {
		return toArray(text, null);
	}

	// 转换为数组
	public static <T> Object[] toArray(String text, Class<T> clazz) {
		return JSON.parseArray(text, clazz).toArray();
	}

	// 转换为List
	public static <T> List<T> toList(String text, Class<T> clazz) {
		return JSON.parseArray(text, clazz);
	}

	/**
	 * 将javabean转化为序列化的json字符串
	 * 
	 * @param keyvalue
	 * @return
	 */
	public static Object beanToJson(KeyValue keyvalue) {
		String textJson = JSON.toJSONString(keyvalue);
		Object objectJson = JSON.parse(textJson);
		return objectJson;
	}

	/**
	 * 将string转化为序列化的json字符串
	 * 
	 * @param keyvalue
	 * @return
	 */
	public static Object textToJson(String text) {
		Object objectJson = JSON.parse(text);
		return objectJson;
	}

	/**
	 * json字符串转化为map
	 * 
	 * @param s
	 * @return
	 */
	public static Map<String, Object> stringToCollect(String s) {
		Map<String, Object> m = JSONObject.parseObject(s);
		return m;
	}

	/**
	 * 将map转化为string
	 * 
	 * @param m
	 * @return
	 */
	public static String collectToString(Map<String, Object> m) {
		String s = JSONObject.toJSONString(m);
		return s;
	}

	public static <T> T toBean(JSON json, Class<T> clazz) {
		return JSON.toJavaObject(json, clazz);
	}

	public static void main(String[] args) {
		List<User> list = new ArrayList<User>();
		list.add(new User(1, "a"));
		list.add(new User(2, "b"));
		list.add(new User(3, "c"));
		list.add(new User(4, "d"));
		String ret = JsonUtils.toJSONString(list);
		System.out.println(ret);
		List<User> list2 = JsonUtils.toList(ret, User.class);
		System.out.println(list2.size());

		System.out.println("==========复合对象============");
		Blog blog = new Blog("博客1", new User(1, "ajie"));
		String str = JsonUtils.toJSONString(blog);
		System.out.println(str);
		Blog blo = JsonUtils.toBean(str, Blog.class);
		Object obj = blo.getUser();
		User bean = JsonUtils.toBean(obj.toString(), User.class);
		System.out.println(bean.getId());
	}

}

class Blog {
	private String content;
	private Object user;

	public Blog(String content, Object user) {
		this.content = content;
		this.user = user;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Object getUser() {
		return user;
	}

	public void setUser(Object user) {
		this.user = user;
	}

}

class User {
	private String name;
	private int id;

	public User() {

	}

	public User(int id, String name) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
