package com.ajie.chilli.cache.redis;

/**
 * @author niezhenjie
 */
public interface RedisClient {

	/**
	 * 保存一个键值对
	 * 
	 * @param key
	 * @param value
	 * @throws RedisException
	 */
	String set(String key, Object value) throws RedisException;

	/**
	 * 保存一个键值对
	 * 
	 * @param key
	 * @param value
	 */
	String set(String key, String value) throws RedisException;

	/**
	 * 获取指定键的值
	 * 
	 * @param key
	 * @return
	 * @throws RedisException
	 */
	Object getAsBean(String key) throws RedisException;

	/**
	 * 获取指定键的值
	 * 
	 * @param key
	 * @param clazz
	 * @return
	 * @throws RedisException
	 */
	<T> T getAsBean(String key, Class<T> clazz) throws RedisException;

	/**
	 * 获取指定键的值
	 * 
	 * @param key
	 * @return
	 */
	String get(String key);

	/**
	 * 删除指定键
	 * 
	 * @param key
	 */
	boolean del(String key);

	/**
	 * 设置或更新过期时间
	 * 
	 * @param seconds
	 */
	long expire(String key, int seconds);

	/**
	 * hash键值
	 * 
	 * @param field
	 * @param key
	 * @param value
	 * @throws RedisException
	 */
	long hset(String key, String field, Object value) throws RedisException;

	/**
	 * hash键值
	 * 
	 * @param field
	 * @param key
	 * @param value
	 */
	long hset(String key, String field, String value);

	/**
	 * 获取hash键值
	 * 
	 * @param field
	 * @param key
	 * @return
	 */
	String hget(String key, String field);

	/**
	 * 获取hash键值
	 * 
	 * @param field
	 * @param key
	 * @return
	 * @throws RedisException
	 */
	Object hgetAsBean(String key, String field) throws RedisException;

	/**
	 * 获取hash键值
	 * 
	 * @param field
	 * @param key
	 * @return
	 * @throws RedisException
	 */
	<T> T hgetAsBean(String key, String field, Class<T> clazz) throws RedisException;

	/**
	 * 删除hash域中的指定的key
	 * 
	 * @param field
	 * @param key
	 */
	boolean hdel(String key, String field);

	/**
	 * 删除整个hash域，其实就是del
	 * 
	 * @param field
	 */
	boolean hdel(String key);

	/**
	 * 过期剩余时间
	 * 
	 * @param key
	 * @return
	 */
	long ttl(String key);

}
