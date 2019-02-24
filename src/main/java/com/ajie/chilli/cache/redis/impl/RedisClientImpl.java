package com.ajie.chilli.cache.redis.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.ajie.chilli.cache.redis.RedisClient;
import com.ajie.chilli.cache.redis.RedisException;
import com.ajie.chilli.utils.common.JsonUtils;
import com.ajie.chilli.utils.common.StringUtils;

/**
 * RedisClient工具类的实现。在需要使用redis的项目引入dao包，在spring中配置jedisPool连接池<br>
 * 和RedisClientImpl bean，并将jedisPool注入<br>
 * 
 * <p>
 * 例如：
 * 
 * <bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig"><br>
 * ……<br>
 * </bean><br>
 * <bean id="jedisPool" class="redis.clients.jedis.JedisPool"><br>
 * ……<br>
 * <constructor-arg name="poolConfig" ref="jedisPoolConfig"></constructor-arg><br>
 * </bean><br>
 * <bean id="redisClientImpl" class="com.ajie.dao.redis.impl.RedisClientImpl"><br>
 * <constructor-arg name="jedisPool" ref="jedisPool" /><br>
 * </bean>
 * </p>
 * 
 * @author niezhenjie
 */
public class RedisClientImpl implements RedisClient {
	private static final Logger logger = LoggerFactory.getLogger(RedisClientImpl.class);

	/**
	 * 为了兼容更多的场景，这里使用构造注入而不适用注解注入
	 */
	protected JedisPool jedisPool;

	public RedisClientImpl(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	@Override
	public String set(String key, Object value) throws RedisException {
		String val = JsonUtils.toJSONString(value);
		return set(key, val);
	}

	@Override
	public String set(String key, String value) throws RedisException {
		assertKV(key, value);
		try {
			Jedis jedis = jedisPool.getResource();
			String ret = jedis.set(key, value);
			jedis.close();
			return ret;
		} catch (Exception e) {
			throw new RedisException("无法加入redis缓存{key:" + key + ",value:" + value + "}", e);
		}
	}

	@Override
	public String get(String key) {
		String ret = null;
		try {
			Jedis jedis = jedisPool.getResource();
			ret = jedis.get(key);
			jedis.close();
		} catch (Exception e) {
			logger.error("无法从redis中获取值", e);
		}
		return ret;

	}

	@Override
	public Object getAsBean(String key) throws RedisException {
		String str = get(key);
		try {
			Object ret = JsonUtils.toBean(str);
			return ret;
		} catch (Exception e) {
			logger.error("无法将字串【" + str + "】转换为bean对象", e);
		}
		return null;
	}

	@Override
	public <T> T getAsBean(String key, Class<T> clazz) throws RedisException {
		String str = get(key);
		try {
			T ret = JsonUtils.toBean(str, clazz);
			return ret;
		} catch (Exception e) {
			throw new RedisException("无法将字串【" + str + "】转换为bean对象", e);

		}
	}

	@Override
	public boolean del(String key) throws RedisException {
		try {
			Jedis jedis = jedisPool.getResource();
			long ret = jedis.del(key);
			jedis.close();
			return ret > 0;
		} catch (Exception e) {
			throw new RedisException("删除redis值异常", e);
		}
	}

	@Override
	public long expire(String key, int seconds) throws RedisException {
		try {
			Jedis jedis = jedisPool.getResource();
			long ret = jedis.expire(key, seconds);
			jedis.close();
			return ret;
		} catch (Exception e) {
			throw new RedisException(e);
		}

	}

	@Override
	public long hset(String key, String field, Object value) throws RedisException {
		String str = null;
		try {
			str = JsonUtils.toJSONString(value);
			return hset(key, field, str);
		} catch (Exception e) {
			throw new RedisException("无法加入redis缓存{key:" + key + ",field:" + field + ",value:"
					+ value + "}", e);
		}
	}

	@Override
	public long hset(String key, String field, String value) throws RedisException {
		assertKV(field, value);
		try {
			Jedis jedis = jedisPool.getResource();
			long ret = jedis.hset(key, field, value);
			jedis.close();
			return ret;
		} catch (Exception e) {
			throw new RedisException("无法加入redis缓存{key:" + key + ",field:" + field + ",value:"
					+ value + "}", e);
		}

	}

	@Override
	public String hget(String key, String field) {
		try {
			Jedis jedis = jedisPool.getResource();
			String ret = jedis.hget(key, field);
			jedis.close();
			return ret;
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}

	@Override
	public Object hgetAsObject(String key, String field) throws RedisException {
		String str = hget(key, field);
		try {
			Object ret = JsonUtils.toBean(str);
			return ret;
		} catch (Exception e) {
			logger.error("无法将字串【" + str + "】转换为bean对象", e);
		}
		return null;
	}

	@Override
	public <T> T hgetAsBean(String key, String field, Class<T> clazz) throws RedisException {
		String str = hget(key, field);
		try {
			T ret = JsonUtils.toBean(str, clazz);
			return ret;
		} catch (Exception e) {
			throw new RedisException("无法将字串【" + str + "】转换为bean对象", e);
		}
	}

	@Override
	public boolean hdel(String key, String field) throws RedisException {
		try {
			Jedis jedis = jedisPool.getResource();
			long ret = jedis.hdel(key, field);
			jedis.close();
			return 0 < ret;
		} catch (Exception e) {
			throw new RedisException("删除redis值异常", e);
		}
		
	}

	@Override
	public boolean hdel(String key) throws RedisException {
		return del(key);
	}

	@Override
	public long ttl(String key) {
		Jedis jedis = jedisPool.getResource();
		long ttl = jedis.ttl(key);
		jedis.close();
		return ttl;
	}

	protected void assertKV(String key, String val) {
		if (StringUtils.isEmpty(key))
			throw new IllegalArgumentException("key不能为空");
		if (StringUtils.isEmpty(val)) {
			throw new IllegalArgumentException("key不能为空");
		}
	}

	@Override
	public long incr(String key) {
		Jedis jedis = jedisPool.getResource();
		long ttl = jedis.incr(key);
		jedis.close();
		return ttl;
	}

	@Override
	public long decr(String key) {
		Jedis jedis = jedisPool.getResource();
		long ttl = jedis.decr(key);
		jedis.close();
		return ttl;
	}

	public Jedis getResource() {
		try {
			Jedis jedis = jedisPool.getResource();
			return jedis;
		} catch (Exception e) {
			logger.error("无法打开redis客户端", e);
		}
		return null;
	}

}
