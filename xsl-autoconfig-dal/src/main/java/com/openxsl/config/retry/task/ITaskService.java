package com.openxsl.config.retry.task;

import java.util.List;

import com.openxsl.config.dal.jdbc.QueryMap;
import com.openxsl.config.exception.ServiceException;
import com.openxsl.config.rpcmodel.Arguments;

public interface ITaskService {
	
	/**
	 * 创建任务及明细（第一步）
	 * @param arguments
	 * @return
	 */
	public String createTask(Arguments arguments);
	
	/**
	 * 新建一条操作日志，并修改任务的步骤和状态；如有必要，修改明细表
	 * @param operation     操作信息
	 * @param taskValues    任务表的属性(除status、stepId、lastModified外）
	 * @param details       任务明细，至少包含“orderNo”属性（json）
	 * @throws FundServiceException
	 */
	public void updateAndLog(TaskOperation operation, QueryMap<?> taskValues,
						List<String> details) throws ServiceException;
	
	/**
	 * Task表保存的返回结果，若result不为空，保存到TaskOperation
	 * @param context 	  上下文（包括：任务ID、response、isAysnc及其他属性）
	 * @param result     返回结果
	 */
	public void saveAsyncResponse(String respId, int status, String callbackBody)
					throws ServiceException;
	
	/**
	 * 通过响应ID查找最新一条任务
	 * @param respId  response的ID
	 * @return Task信息
	 */
	public Task findByRespId(String respId);
	
	/**
	 * 缓存最后结果
	 * @param result
	 */
	public void putCacheResult(Object result);
	
	/**
	 * 构建TaskOperation对象（不插入）
	 */
	public TaskOperation newOperation(String taskId, String parameter, String stepId,
							int fails, String result);

}
