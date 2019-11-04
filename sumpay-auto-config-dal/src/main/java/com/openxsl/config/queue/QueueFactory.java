package com.openxsl.config.queue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiongsl
 */
public class QueueFactory {
	static Map<String, TaskQueue> queueCtx = new HashMap<String, TaskQueue>();
	
	public static void put(TaskQueue queue){
		if (queue == null) {
			return;
		}
		final String name = queue.getName();
		if (queueCtx.containsKey(name)){
			throw new IllegalStateException("Replicate queue-name:"+name);
		}
		
		queueCtx.put(name, queue);
	}
	
	public static TaskQueue getQueue(String name){
		if (!queueCtx.containsKey(name)){
			throw new IllegalArgumentException("Not exists queue named:"+name);
		}
		return queueCtx.get(name);
	}
	
}
