package com.openxsl.config.retry.task;

import java.util.List;

import com.openxsl.config.exception.ServiceException;
import com.openxsl.config.exception.SkipRetryException;

public interface StateMachine {
	public static final int STS_START = 0;     //default
	public static final int STS_SUCC = 1;
	public static final int STS_FAIL = 2;
	public static final int STS_DISCARD = -1;
	public static final int STS_SUSPEND = 10;  //可疑状态，也可理解为业务处理失败
	
	/**
	 * 最后修改操作的状态
	 * @param taskId
	 * @param operationId
	 * @param status （成功 Or 失败）
	 * @throws SkipRetryException
	 */
	public void modifyStatus(String taskId, long operationId, int status)
				throws SkipRetryException;
	
	/**
	 * 丢弃一个任务
	 * @param taskId
	 * @throws SkipRetryException
	 */
	public void disable(String taskId) throws ServiceException;
	
	/**
	 * Task表保存调用的返回结果，若result不为空，保存到TaskOperation
	 * @param context 	  上下文（包括：任务ID、response、isAysnc及其他属性）
	 * @param result     返回结果
	 */
	public void saveTaskResp(TaskContext context, Object result) throws ServiceException;
	
	/**
	 * 重新执行失败任务时，查询一个任务在某一步的输入参数
	 * @param stepId  步骤ID
	 * @param taskId  任务ID，如果不传则返回多个任务的
	 * @return
	 * @throws FundServiceException
	 */
	public List<String> getFailedParameters(String stepId, String... taskId);
	
	/**
	 * 根据订单ID获取执行任务的情况，用于判断重复提交
	 */
	public Task findTaskByBizId(String serviceId, String bizSys, String orderNo);

}
