package com.openxsl.config.redis.tracing;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.openxsl.config.filter.ListableTracingFilter;

/**
 * 具有调用链功能的RedisTemplate实现。
 * @param <K>  Key类型
 * @param <V>  Value类型
 * 
 * @author xiongsl
 * @modify 2019/02/14 采用AOP-RedisHelperTracingAdvisors取 key
 * @Deprecated 2019/02/15 被trace-api/BeanPostProcessor代理了，因此可以被废弃
 */
public class FlexibleRedisTemplate<K, V> extends RedisTemplate<K, V> {
	private static final ListableTracingFilter FILTERS = new ListableTracingFilter();
	private final String redisUrl;
	private boolean tracing;
	
	static {
		FILTERS.load("redis");
	}
	
	public FlexibleRedisTemplate(String redisUrl, boolean tracing) {
		this.redisUrl = redisUrl;
		this.tracing = tracing;
		this.setKeySerializer(new FlexibleKeySerializer());
	}
	
	/**
	 * 获取Redis连接的后续操作：拿到接口的方法名和Key参数
	 * 
	 * RedisTemplate的调用栈：
	 *    opsForValue.set() | opsForHash | opsForList     5
	 *       AbstractOperations.execute(RedisCallback<T> callback, true)   4
	 *          RedisTemplate.execute(RedisCallback<T> callback, true)     3
	 *             RedisTemplate.execute(RedisCallback<T> callback, true, false)   2
	 *                RedisConnectionUtils.getConnection(factory);
	 *                preProcessConnection(conn);    **1
	 *                callback.doInRedis(conn);
	 *                postProcessResult(result,conn,boolean);  **
	 *                RedisConnectionUtils.releaseConnection(conn, factory);
	 */
	@Override
	protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
		if (shouldTracing()) {
			//改为 RedisHelperTracingAdvisors$RedisHelperAdvice
			String method = RedisOptsContext.getMethod(true);
			Object key = RedisOptsContext.getKey(true);
			FILTERS.before(redisUrl, method, key);
//			StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
//			StackTraceElement stackTrace = null;
//			try {
//				int deep = 6;
//				while (!stacks[deep].getClassName().endsWith("RedisHelper")) {
//					deep --;
//				}
//				while (stacks[deep-1].getClassName().endsWith("RedisHelper")) {
//					deep --;
//				}
//				stackTrace = stacks[deep];
//				String method = stackTrace.getMethodName();  //keys, hasKeys
//				Object keys = ((FlexibleKeySerializer)this.getKeySerializer()).getKey();
//				boolean flag = keys != null && !method.endsWith("Inner");
//				if (flag) {  //List: trim(),index()
//					COMMAND.set(Boolean.TRUE);
//					FILTERS.before(redisUrl, method, keys);
//				}
//			} finally {
//				stackTrace = null;
//				stacks = null;
//			}
		}
		return connection;
	}
	
	/**
	 * 完成Redis读写后的操作
	 */
	@Override
	protected <T> T postProcessResult(T result, RedisConnection conn, boolean existingConnection) {
		if (shouldTracing()) {
			FILTERS.after(result);
		}
		return result;
	}
	
	public void setTracing(boolean flag) {
		this.tracing = flag;
	}
	private final boolean shouldTracing() {
		return tracing;
	}
//	@SuppressWarnings("unchecked")
//	public void ensureTracing(String key) {
//		if (tracing) {
//			((FlexibleKeySerializer)this.getKeySerializer()).setKey(key);
//		}
//	}
	
	/**
	 * 收集Key放在ThreadLocal中
	 * @author xiongsl
	 * @param <T>
	 */
	public class FlexibleKeySerializer //extends Jackson2JsonRedisSerializer<String>
				implements RedisSerializer<String>{
//		private ThreadLocal<Object> operKey = new ThreadLocal<Object>();
		private ObjectMapper objectMapper = new ObjectMapper();
		
		@Override
		public byte[] serialize(String key) throws SerializationException {
//			if (key == null) {
//				return SerializationUtils.EMPTY_ARRAY;
//			}
//			this.setKey(key);
			try {
				return this.objectMapper.writeValueAsBytes(key);
			} catch (Exception ex) {
				throw new SerializationException("Could not write JSON: " + ex.getMessage(), ex);
			}
		}
		@Override
		public String deserialize(byte[] bytes) throws SerializationException {
//			if (bytes == null || bytes.length < 1) {
//				return null;
//			}
			try {
				return this.objectMapper.readValue(bytes, 0, bytes.length, String.class);
			} catch (Exception ex) {
				throw new SerializationException("Could not read JSON: " + ex.getMessage(), ex);
			}
		}
		
//		public Object getKey() {
//			Object key = operKey.get();
//			operKey.remove();
//			return key;
//		}
//		
//		public void setKey(String key) {
//			if (key != null) {
//				operKey.set(key);
//			}
//		}

	}
	
}
