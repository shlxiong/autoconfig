package com.openxsl.config.redis;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

/**
 * <pre>
 * Redis操作类(Object，包括Map、Set、List，但只能作为一个整体来操作)
 * 如果希望保存的对象有生命周期，则save(...)前需调用setExpires()
 * </pre>
 * @author xiongsl
 */
public class GenericRedisHelper<T extends Serializable> { //implements RedisHelper<Serializable> {
	private RedisTemplate<String, Serializable> template;
	private String toStr = "${className}@${hashCode}(entity=${0}, expires=${1})";
	private boolean inited = false;
	
	public void setTemplate(RedisTemplate<String,Serializable> template) {
		this.template = template;
//		ParameterizedType pt = (ParameterizedType)getClass().getGenericInterfaces()[0];
//		Class<T> entityClass = (Class<T>)pt.getActualTypeArguments()[0];
//		this.setEntityClass(entityClass);
	}
	public void setEntityClass(Class<T> entityClass) {
		if (!inited) {
			template.setKeySerializer(template.getStringSerializer());
			//下面使用json格式，默认JdkSerializationRedisSerializer
			template.setValueSerializer(new Jackson2JsonRedisSerializer<T>(entityClass));
			toStr = toStr.replace("${0}", entityClass.getName())
					.replace("${className}", getClass().getName())
					.replace("${hashCode}", String.valueOf(this.hashCode()));
			inited = true;
		}
	}
	public RedisTemplate<String, Serializable> getRedisTemplate(){
		return this.template;
	}
	
	private long expires = -1;
	public void setExpires(long seconds){
		this.expires = seconds;
	}
	
	/**
	 * 保存一个变量值到缓存中。如果之前设置了setExpires()，则具有生命周期
	 * @param key
	 * @param entity
	 */
	public void save(String key, Serializable entity){
		this.ensureSave(key);
		if (expires == -1){
			//opsForValue().set(key, entity) 最终会调用 template.execute(ReidsCallback)
			template.opsForValue().set(key, entity);
		}else{
			template.opsForValue().set(key, entity, expires, TimeUnit.SECONDS);
		}
	}
	/**
	 * 保存一个缓存值，指定生存周期（不受全局变量expires的影响）
	 */
	public void save(String key, T entity, long seconds){
		this.ensureSave(key);
		template.opsForValue().set(key, entity, seconds, TimeUnit.SECONDS);
	}
	
	/**
	 * 当不存在时，才设置缓存值（初始化）
	 */
	public boolean saveIfAbsent(String key, T entity) {
		this.ensureSave(key);
		return template.opsForValue().setIfAbsent(key, entity);
	}
	
	/**
	 * 先拿出来（旧值），再更新
	 */
	@SuppressWarnings("unchecked")
	public T getAndSet(String key, T entity) {
		return (T)template.opsForValue().getAndSet(key, entity);
	}
	/**
	 * 批量保存，每个Map.Entry单独保存
	 * @param entityMap 键-值
	 */
	public void save(Map<String, T> entityMap){
		Assert.notEmpty(entityMap, "entityMap is empy");
		for (String key : entityMap.keySet()) {
			this.ensureSave(key);
		}
		if (expires == -1){
			template.opsForValue().multiSet(entityMap);
		}else{
			for (Map.Entry<String, T> entry : entityMap.entrySet()){
				template.opsForValue().set(entry.getKey(), entry.getValue(),
									expires, TimeUnit.SECONDS);
			}
		}
	}
	
	/**
	 * 递增一个变量，并返回值。如果开始不存在，则值为step。
	 * @param key 关键字
	 * @param step 步数（Long或Double）
	 * @param seconds 指定生命周期，不传则为永久
	 * @return 递增后的结果
	 */
	public Number increaseOrDecr(String key, Number step, long... seconds) {
		Assert.notNull(step, "'step' can NOT be null");
		if (seconds.length > 0 && !this.existsInner(key)) {
			template.opsForValue().set(key, step, seconds[0], TimeUnit.SECONDS);
			return step;
		}
		if (step instanceof Long || step instanceof Integer){
			return template.opsForValue().increment(key, Long.parseLong(step.toString()));
		}else {
			return template.opsForValue().increment(key, Double.parseDouble(step.toString()));
		}
	}
	
//	@Override
	public void delete(String id){
		if (id == null) {
			throw new IllegalArgumentException("Key is empty");
		}
		if (id.contains("*") || id.contains("?")) {
			throw new IllegalArgumentException("Key cant NOT contain *|?");
		}
		template.delete(id);
	}
//	@Override
	public void deleteAll(Collection<String> ids){
		for (String id : ids) {
			if (id.contains("*") || id.contains("?")) {
				throw new IllegalArgumentException("Key cant NOT contain *|?");
			}
		}
		//下面会做判断：CollectionUtils.isEmpty(keys)
		template.delete(ids);
	}
	/**
	 * 模糊删除（*|?）
	 * @param pattern
	 */
//	@Override
	public void deleteLike(String pattern) {
		template.delete(this.keysInner(pattern));
	}
	
	/**
	 * 从缓存中取值，如果传参seconds则更新存活时间
	 * @param id 关键字
	 * @param seconds 缓存的生命周期
	 */
	@SuppressWarnings("unchecked")
	public T get(String id, long... seconds) {
//		((FlexibleRedisTemplate<String,T>)template).ensureTracing(id); //tracing
		T entity = (T)template.opsForValue().get(id);
		if (entity != null && seconds.length > 0) {
			template.expire(id, seconds[0], TimeUnit.SECONDS);
		}
		return entity;
	}
	@SuppressWarnings("unchecked")
	public List<T> get(Collection<String> ids){
		return (List<T>)template.opsForValue().multiGet(ids);
	}
	/**
	 * 临时查一下其他简单类型（非entityClass）的数据
	 */
	@SuppressWarnings({"unchecked" })
	public synchronized <E> E getObject(String id, Class<E> valueType) {
		RedisSerializer<?> temp = template.getValueSerializer();
		try {
			template.setValueSerializer(new Jackson2JsonRedisSerializer<E>(valueType));
//			((FlexibleRedisTemplate<String,T>)template).ensureTracing(id);  //tracing
			return (E)template.opsForValue().get(id);
		}finally {
			template.setValueSerializer(temp);
		}
	}
	
	public Boolean updateExpires(String key, long seconds) {
		return template.expire(key, seconds, TimeUnit.SECONDS);
	}

	/**
	 * 模糊查询（支持？*）
	 * @param pattern
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> selectLike(String pattern){
		return (List<T>)template.opsForValue().multiGet(this.keysInner(pattern));
	}
	
	public String toString() {
		return toStr.replace("${1}", String.valueOf(expires));
	}
	
	private final Collection<String> keysInner(String pattern){
		return template.keys(pattern);
	}
	private final boolean existsInner(String key) {
		return template.hasKey(key);
	}
	
	/**
	 * 提示先初始化，判断key长度
	 */
	private final void ensureSave(String key) {
		if (!inited) {
			throw new IllegalStateException("Please setEntityClass() before save operation");
		}
		if (key==null || key.equals("")) {
			throw new IllegalArgumentException("Key is empty");
		}
		if (key.length() > 255) {
			throw new IllegalArgumentException("Key is too large!");
		}
		if (key.contains("*") || key.contains("?")) {
			throw new IllegalArgumentException("Key NOT contains *|?");
		}
		//TODO 限制namespace
	}

}
