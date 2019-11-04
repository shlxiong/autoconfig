package com.openxsl.config.redis.tracing;

/**
 * RedisTemplate上下文，保存操作Key
 * @author xiongsl
 */
public class RedisOptsContext {
	private static ThreadLocal<Object> operKey = new ThreadLocal<Object>();
	private static ThreadLocal<String> method = new ThreadLocal<String>();
	
	public static Object getKey(boolean remove) {
		Object key = operKey.get();
		if (remove) {
			operKey.remove();
		}
		return key;
	}
	public static void setKey(Object key) {
		operKey.set(key);
	}
	public static void removeKey() {
		operKey.remove();
	}
	
	public static String getMethod(boolean remove) {
		String name = method.get();
		if (remove) {
			method.remove();
		}
		return name;
	}
	public static void setMethod(String name) {
		method.set(name);
	}
	public static void removeMethod() {
		method.remove();
	}
}
