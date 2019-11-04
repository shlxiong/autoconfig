package com.openxsl.config.util;

import java.util.Calendar;

/**
 * 有生命周期的对象
 * @author xiongsl
 * @param <T>
 */
public class PeriodicEntity<T> {
	private T entity;
	private long ttl;    //存活时间（秒）
	private long birth = System.currentTimeMillis();
	
	public boolean expires() {
		if (System.currentTimeMillis()-birth > ttl*1000) {
			entity = null;
			return true;
		}
		return false;
	}
	public void expiresOnTomorrow() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		ttl = (calendar.getTime().getTime()-System.currentTimeMillis()) / 1000;
	}
	public void expiresOnNextMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		ttl = (calendar.getTime().getTime()-System.currentTimeMillis()) / 1000;
	}
	
	public T getEntity() {
		return entity;
	}
	public void setEntity(T entity) {
		this.entity = entity;
	}
	public long getTtl() {
		return ttl;
	}
	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

}
