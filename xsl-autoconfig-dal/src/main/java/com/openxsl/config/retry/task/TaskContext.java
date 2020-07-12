package com.openxsl.config.retry.task;

import java.util.List;

import com.openxsl.config.rpcmodel.QueryMap;

/**
 * 任务上下文
 * @author xiongsl
 */
public class TaskContext {
	public static final int MAX_LEN_PARAMETER = 2047;
	public static final int MAX_LEN_RESULT = 255;
	
	private static ThreadLocal<String> taskId = new ThreadLocal<String>();
	private long operationId;
	private String response;
	private int status;
	private boolean async;
	private QueryMap<Object> values = new QueryMap<Object>(2);
	private List<String> details;
	
	public static String getTaskId() {
		return taskId.get();
	}
	public static void setTaskId(String tid) {
		taskId.set(tid);
	}
	public static void removeTaskId() {
		taskId.remove();
	}
	public boolean isAsync() {
		return async;
	}
	public void setAsync(boolean async) {
		this.async = async;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public QueryMap<?> getValues() {
		return values;
	}
	public void setValues(QueryMap<Object> values) {
		this.values = values;
	}
	public void put(String name, Object value) {
		this.values.put(name, value);
	}
	public Object remove(String name) {
		return this.values.remove(name);
	}
	public long getOperationId() {
		return operationId;
	}
	public void setOperationId(long operationId) {
		this.operationId = operationId;
	}
	public List<String> getDetails() {
		return details;
	}
	public void setDetails(List<String> details) {
		this.details = details;
	}
}
