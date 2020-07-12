package com.openxsl.config.rpcmodel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.exception.ErrorCodes;
import com.openxsl.config.thread.GrouppedThreadFactory;

/**
 * 取同步调用的结果
 * @author 001327-xiongsl
 */
@ScanConfig
public class ResultCache {
    private final long TIME_OUT = 30000;
	private Map<String, ResultQueue> queueMap = new ConcurrentHashMap<String, ResultQueue>();
	@Value("${resultcache.timeout:30000}")
	private long waits = 0;
	@Value("${resultcache.errormsg:由于网络原因或外部系统繁忙，尚未收到对方响应}")
	private String errorMsg;
	
	private static ResultCache instance;
	
	public synchronized static ResultCache getInstance() {
		if (instance == null) {
			instance = new ResultCache();
		}
		return instance;
	}
	
	public ResultCache(){
		instance = this;
		GrouppedThreadFactory.newThread("CacheResult-Cleaner", true, new Runnable(){
			@Override
			public void run(){
				while (true){
					try {
						Thread.sleep(TIME_OUT / 2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					final long now = System.currentTimeMillis();
					for (Map.Entry<String, ResultQueue> entry : queueMap.entrySet()){
						if (entry.getValue().timestamp+TIME_OUT < now){
							queueMap.remove(entry.getKey());
						}
					}
				}
			}
		}).start();
	}
	
	/**
	 * 2016-11-01   防止ReceiveService卡死
	 */
	public boolean save(Result result) {
		try {
			String taskId = result.getTaskId();
			if (queueMap.containsKey(taskId)) {
				queueMap.get(taskId).put(result);
				return true;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Result get(String taskId) {
		if (!queueMap.containsKey(taskId)) {
			queueMap.put(taskId, new ResultQueue());
		}
		try {
			Result result = queueMap.get(taskId).poll(waits, TimeUnit.MILLISECONDS);
			if (result == null){
				result = new Result(taskId, ErrorCodes.TIMEOUT.code(), null);
				result.setMessage("请求超时");
				result.setException(errorMsg);
			}
			return result;
		} catch(InterruptedException ie) {
			return null;
		} finally {
			queueMap.remove(taskId);
		}
	}
	
	@SuppressWarnings("serial")
	private class ResultQueue extends SynchronousQueue<Result>{
		private long timestamp = System.currentTimeMillis();
	}
	
}
