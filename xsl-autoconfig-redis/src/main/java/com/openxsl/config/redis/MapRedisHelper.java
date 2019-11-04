package com.openxsl.config.redis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.openxsl.config.util.PeriodicEntity;

/**
 * Map值类型的RedisHelper
 * @author xiongsl
 */
@SuppressWarnings("unchecked")
public class MapRedisHelper<T extends Serializable> {
	private RedisTemplate<String, Serializable> template;
	
	@SuppressWarnings({"rawtypes" })
	@PostConstruct
	public void init() {
		template.setKeySerializer(template.getStringSerializer());
		template.setValueSerializer(new Jackson2JsonRedisSerializer(HashMap.class));
		template.setHashKeySerializer(template.getStringSerializer());
		template.setHashValueSerializer(new Jackson2JsonRedisSerializer(Object.class));
	}
	
	/**
	 * 保存一个有生命周期的对象
	 */
	public void save(String cacheKey, PeriodicEntity<Map<String,T>> periodEntity) {
		template.opsForHash().putAll(cacheKey, periodEntity.getEntity());
		if (periodEntity.getTtl() > 0) {
			template.expire(cacheKey, periodEntity.getTtl(), TimeUnit.SECONDS);
		}
	}
	
	/**
	 * 将Map键值追加到Redis中
	 * @see {@link java.util.Map#putAll(Map)}
	 */
	public void putAll(String cacheKey, Map<String,T> entries) {
		template.opsForHash().putAll(cacheKey, entries);
	}
	
	/**
	 * 取出Redis对应变量名的Map对象
	 * @param cacheKey 缓存Key
	 */
	public Map<String,T> get(String cacheKey){
		Map<Object,Object> result = template.opsForHash().entries(cacheKey);
		Map<String, T> target = new HashMap<String, T>(result.size());
		for (Map.Entry<Object,Object> entry : result.entrySet()) {
			target.put((String)entry.getKey(), (T)entry.getValue());
		}
		result.clear();
		return target;
	}
	
	/**
	 * 取Redis中Map的一个Entry值
	 * @param cacheKey 缓存Key
	 * @param entryKey
	 * @seee {@link java.util.Map#get(Object)}
	 */
	public T get(String cacheKey, String entryKey, T... defValue) {
		T value = (T)template.opsForHash().get(cacheKey, entryKey);
		if (value==null && defValue.length>0) {
			return defValue[0];
		} else {
			return value;
		}
	}
	/**
	 * 设值
	 * @param cacheKey 缓存Key
	 * @param entryKey
	 * @param value
	 * @seee {@link java.util.Map#put(Object, Object)}
	 */
	public void put(String cacheKey, String entryKey, T value) {
		template.opsForHash().put(cacheKey, entryKey, value);
	}
	/**
	 * 删除Redis中Map的多个Entry
	 * @param cacheKey 缓存Key
	 * @param entryKey
	 * @see {@link java.util.Map#remove(Object)}
	 */
	public Long remove(String cacheKey, String... entryKey) {
		Object[] args = Arrays.copyOf(entryKey, entryKey.length, Object[].class);
		return template.opsForHash().delete(cacheKey, args);
	}
	/**
	 * 清除Redis中Map的所有值
	 * @param cacheKey 缓存Key
	 * @see java.util.Map#clear()
	 */
	public void clear(String cacheKey) {
		template.delete(cacheKey);
	}
	
	public int size(String cacheKey) {
		return template.opsForHash().size(cacheKey).intValue();
	}
	
	/**
	 * 如果不存在，则插入缓存（不保证原子性）
	 * @see GenericRedisHelper#saveIfAbsent(String, Serializable)
	 */
	public boolean saveIfAbsent(String key, Map<String, T> entity) {
		if (!template.hasKey(key)) {
			template.opsForHash().putAll(key, entity);
			return true;
		}
		return false;
	}

	/**
	 * 递增Map中的某一个Entry，估计不常用
	 * @see GenericRedisHelper#increaseOrDecr(String, Number)
	 */
	public Number increaseOrDecr(String cacheKey, String entryKey, Number step) {
		if (step != null) {
			if (step instanceof Long || step instanceof Integer) {
				return template.opsForHash().increment(cacheKey, entryKey, Long.parseLong(step.toString()));
			}else {
				return template.opsForHash().increment(cacheKey, entryKey, Double.parseDouble(step.toString()));
			}
		} else {
			return (Number)template.opsForHash().get(cacheKey, entryKey);
		}
	}
	public Map<String, T> getAndSet(String key, Map<String, T> entity) {
		try{
			return this.get(key);
		}finally {
			this.clear(key);
			this.putAll(key, entity);
		}
	}
	
	public RedisTemplate<String, ?> getRedisTemplate() {
		return template;
	}
	public void setTemplate(RedisTemplate<String,Serializable> template) {
		this.template = template;
	}

}
