package com.openxsl.config.retry;

import java.io.Serializable;
import java.util.List;

import com.openxsl.config.exception.ServiceException;
import com.openxsl.config.queue.TaskQueue;

public interface Executable<P, R> extends Serializable {

	/**
	 * 执行业务操作
	 * @param args 参数
	 * @param fails 失败次数
	 * @return 下一步的输入参数
	 * @throws FundServiceException
	 */
	R execute(P args, int fails) throws ServiceException;
	
	/**
	 * 加载初始数据
	 * @return
	 */
	List<P> load();
	
	/**
	 * 执行成功的队列（下一步）
	 */
	TaskQueue getSuccQueue();
	
	/**
	 * 失败队列
	 */
	TaskQueue getFailQueue();

}
