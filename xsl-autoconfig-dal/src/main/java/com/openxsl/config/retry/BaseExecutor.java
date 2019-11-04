package com.openxsl.config.retry;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.openxsl.config.exception.ServiceException;
import com.openxsl.config.exception.SkipRetryException;
import com.openxsl.config.retry.task.ITaskService;
import com.openxsl.config.retry.task.StateMachine;
import com.openxsl.config.retry.task.TaskContext;
import com.openxsl.config.util.NetworkUtils;
import com.openxsl.config.util.StringUtils;

/**
 * 持久化参数的执行对象
 * @author xiongsl
 *
 * @param <P>  参数类型
 * @param <R>  结果类型
 */
@SuppressWarnings("serial")
public abstract class BaseExecutor<P, R> implements Executable<P, R> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final String LOCAL_IP = NetworkUtils.LOCAL_IP;
	@Autowired
	protected StateMachine stateMachine;
	@Autowired
	protected ITaskService taskService;

	@Override
	public R execute(P args, int fails) throws ServiceException {
		TaskContext context = new TaskContext();
		logger.info("{} params: {}", this.getStep(),args);
		Object param = this.handleParameter(args, context);
		long operationId = this.saveOperation(args, new Integer(fails).shortValue(), param);
		context.setOperationId(operationId);
		
		R result = null;
		Object saveObj = null;
		try {
			result = this.handleRequest(args, context, param);
			logger.info("{} result: {}", this.getStep(),result);
		} catch (ServiceException se) {
			Throwable cause = (se.getCause()==null) ? se : se.getCause();
			saveObj = StringUtils.getStackTrace(cause);
			throw se;
		} finally {
			saveObj = (result!=null) ? result : saveObj;
			this.postProcess(context, saveObj);
		}
		return result;
	}
	@Override
	public List<P> load() {
		return null;
	}
	
	/**
	 * 处理入参，发生异常不会重试
	 * @param args
	 * @return
	 * @throws SkipRetryException
	 */
	protected abstract Object handleParameter(P args, TaskContext context) throws SkipRetryException;
	/**
	 * 保存操作日志（参数）
	 * @param args   入参
	 * @param fails  第几次
	 * @param param  处理后的参数（#handleParameter的返回值）
	 * @return
	 * @throws ServiceException
	 */
	protected abstract long saveOperation(P args, short fails, Object params) throws ServiceException;
	
	/**
	 * 执行业务操作，将需要修改的Task属性都放到TaskContext中去。
	 * <em>如果没有下一步，请返回空值</em>
	 * @param args  原始参数
	 * @param operationId 操作日志ID（#saveOperation返回值）
	 * @param param  处理后的参数（#handleParameter的返回值）
	 */
	protected abstract R handleRequest(P args, TaskContext context, Object params)
				throws ServiceException;
	
	/**
	 * 修改任务状态，保存处理结果
	 * @param context 任务上下文
	 * @param result  当前步骤的处理结果或异常
	 */
	protected void postProcess(TaskContext context, Object result) {
		stateMachine.modifyStatus(TaskContext.getTaskId(), context.getOperationId(),
								context.getStatus());
		try{
			stateMachine.saveTaskResp(context, result);
    	}catch(ServiceException fe){ //失败也无关紧要
    		logger.warn("failed to save response:", fe);
    	}
	}
	
	protected abstract String getStep();

}
