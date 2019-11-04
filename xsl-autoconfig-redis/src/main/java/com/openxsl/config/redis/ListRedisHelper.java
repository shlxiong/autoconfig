package com.openxsl.config.redis;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.openxsl.config.util.PeriodicEntity;

/**
 * List格式存储，由于range()从左到右，所以采用：rightPush leftPop     
 * @author xiongsl
 */
public class ListRedisHelper<T extends Serializable> {
	private RedisTemplate<String, T> template;
	
	@SuppressWarnings({"rawtypes", "unchecked" })
	@PostConstruct
	public void init() {
		template.setKeySerializer(template.getStringSerializer());
		template.setValueSerializer(new Jackson2JsonRedisSerializer(Object.class));
	}
	
	/**
	 * 取出全部值 
	 * @param key
	 */
	public List<T> get(String key) {
		long size = -1; //template.opsForList().size(key);
		return template.opsForList().range(key, 0, size);
	}
	
	/**
	 * 保存一个有生命周期的对象
	 */
	public void save(String key, PeriodicEntity<List<T>> periodEntity) {
		template.opsForList().rightPushAll(key, periodEntity.getEntity());
		if (periodEntity.getTtl() > 0) {
			template.expire(key, periodEntity.getTtl(), TimeUnit.SECONDS);
		}
	}
	/**
	 * 不存在则插入一个有生命周期的对象
	 */
	public boolean addIfAbsent(String key, PeriodicEntity<List<T>> periodEntity) {
		List<T> list = this.get(key);
		if (list.isEmpty()) {
			this.save(key, periodEntity);
			return true;
		}
		return false;
	}

	/**
	 * 插入一个元素
	 * @see java.util.List#add(Object)
	 */
	public void add(String key, T entity) {
		template.opsForList().rightPush(key, entity);
	}
	/**
	 * 不存在则插入一个元素
	 */
	public boolean addIfAbsent(String key, T entity) {
		List<T> list = this.get(key);
		if (list.isEmpty() || !list.contains(entity)) {
			this.add(key, entity);
			return true;
		}
		return false;
	}
	/**
	 * 在指定位置插入一个元素（不保证原子性）
	 * @see java.util.List#add(int, Object)
	 */
	public void add(String key, int index, T entity) {
		//spring-data-redis 还没有实现jedis.linsert
		long len = -1; //template.opsForList().size(key);
		List<T> list = template.opsForList().range(key, index, len);
		list.add(0, entity);
		template.opsForList().trim(key, 0, index-1);
		template.opsForList().rightPushAll(key, list);
	}
	
	/**
	 * 插入多个元素
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public void addAll(String key, List<T> list) {
		template.opsForList().rightPushAll(key, list);
	}
	/**
	 * 在指定位置插入多个元素（不保证原子性）
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public void addAll(String key, int index, List<T> list) {
		long len = -1; //template.opsForList().size(key);
		List<T> subList = template.opsForList().range(key, index, len);
		subList.addAll(0, list);
		template.opsForList().trim(key, 0, index-1);
		template.opsForList().rightPushAll(key, subList);
	}
	
	/**
	 * 取指定位置的元素
	 * @see java.util.List#get(int)
	 */
	public T get(String key, int index) {
//		((FlexibleRedisTemplate<String,T>)template).ensureTracing(key);
		return template.opsForList().index(key, index);
	}
	/**
	 * @see java.util.List#set(int, Object)
	 */
	public void set(String key, int index, T entity) {
		template.opsForList().set(key, index, entity);
	}
	
	/**
	 * 删除一个元素
	 * @see java.util.List#remove(int)
	 */
	public T remove(String key, int index) {
		if (index == 0) {
			return template.opsForList().leftPop(key);
		} else if (index == -1) {
			return template.opsForList().rightPop(key);
		}
		
		List<T> list = template.opsForList().range(key, index, -1);
		T elt = list.remove(0);
		template.opsForList().trim(key, 0, index-1);
		template.opsForList().rightPushAll(key, list);
		return elt;
	}
	/**
	 * 删除缓存值
	 * @see java.util.List#clear()
	 */
	public void clear(String key) {
		template.delete(key);
	}
	public long size(String key) {
		return template.opsForList().size(key);
	}
	
	public Number increaseOrDecr(String cacheKey, String entryKey, Number step) {
		throw new UnsupportedOperationException("不支持这个操作");
	}
	
	/**
	 * 删除指定范围外的其他值
	 * @param key 缓存Key
	 * @param start 开始位置
	 * @param end 结束位置
	 */
	public void keepOnly(String key, int start, int end) {
//		((FlexibleRedisTemplate<String,T>)template).ensureTracing(key);
		template.opsForList().trim(key, start, end);
	}
//	/**
//	 * 递增缓存的值（不保证原子性）
//	 */
//	public void increaseOrDecr(String cacheKey, int index, Number step) {
//		Number origin = (Number)template.opsForList().index(cacheKey, index);
//		if (step != null && step.intValue() != 0) {
//			if (origin instanceof Long || step instanceof Integer) {
//				long value = origin.longValue() + step.longValue();
//				template.opsForList().set(cacheKey, index, value);
//			}else {
//				//
//			}
//		}
//	}

	public RedisTemplate<String, T> getRedisTemplate() {
		return template;
	}

//	public List<T> getAndSet(String key, List<T> entity) {
//		template.opsForList().rightPopAndLeftPush(key, key);
//		return null;
//	}

	public void setTemplate(RedisTemplate<String, T> template) {
		this.template = template;
	}
	
}
