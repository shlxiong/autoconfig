package com.openxsl.config.queue;

import java.util.List;

import com.openxsl.config.retry.Executable;

/**
 * 任务队列
 * @modify 2018-03-28 setFair()使用优先级队列
 * @author xiongsl
 */
public interface TaskQueue {
	
	public String getName();
	
	public Object get();
	
	public List<Object> getAll();
	
	public boolean put(Object obj);
	
//	public boolean putIfEmpty(Object obj);
	
	@SuppressWarnings("rawtypes")
	public Executable getService();
	
	public void setFair(boolean fair);
	
}
