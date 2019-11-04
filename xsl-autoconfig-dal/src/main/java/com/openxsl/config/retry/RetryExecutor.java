package com.openxsl.config.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openxsl.config.exception.ErrorCodes;
import com.openxsl.config.exception.ServiceException;
import com.openxsl.config.exception.SkipRetryException;
import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.tracing.TracingCollector;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.queue.TaskQueue;
import com.openxsl.config.retry.task.TaskContext;
import com.openxsl.config.rpcmodel.Result;
import com.openxsl.config.rpcmodel.ResultCache;
import com.openxsl.config.thread.tracing.TracingParam;

/**
 * 具有重试功能的任务执行机
 * @author xiongsl
 */
public class RetryExecutor {
    private static Logger logger = LoggerFactory.getLogger(RetryExecutor.class);

	public static <P, R> R execute(Executable<P, R> service, P data, RetryStrategy... strategy) {
        RetryStrategy retry = (strategy.length > 0) ? strategy[0] : RetryStrategy.DEFAULT;
        final long start = System.currentTimeMillis();
        final long timeOut = retry.getTimeout();
//        if (TaskContext.getTaskId() == null) {  //目前在前面Service做了
//        	TaskContext.setTaskId(IDGenerator.getUUID());
//        }
        
        startTracing(service, data);
        
        R result = null;
        Exception lastError = null;
        boolean flag = false;
        final int times = retry.getRetries();
        for (int i = 0; i < times; i++) {
            try {
                result = service.execute(data, i);
                flag = true;
                break;
            } catch (SkipRetryException e) {
                logger.error("Skip to retry", e);
                lastError = e;
                break;
            } catch (ServiceException e) {
            	logger.info("==Retry on-error:", e);
            	lastError = e;
            }

        	if (timeOut > 0 && System.currentTimeMillis()-start > timeOut){
        		break;
        	}else if (i < times-1){
        		try{
            		Thread.sleep(retry.getSleeptime(i));
        		} catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        finishTracing(flag, lastError);

        TaskQueue queue = service.getSuccQueue();
        if (flag){
        	if (result != null && queue != null){  //nextStep
        		queue.put(result);
        	} else if (result != null) {
        		putCacheResult(result);
        	}
        }else if (service.getFailQueue() != null){ //FailQ
        	service.getFailQueue().put(data);
        }else{
        	putCacheResult(lastError);
        }
        TaskContext.removeTaskId();
        return result;
    }
	
	private static void putCacheResult(Object object) {
		String taskId = TaskContext.getTaskId();
		if (taskId == null) {
			taskId = "-999999";
		}
		Result result;
		if (object instanceof Result) {
			result = (Result)object;
		} else if (object instanceof Exception) {
			int code = ErrorCodes.RUNTIME.code();
			if (object instanceof ServiceException) {
				code = ((ServiceException)object).getCode();
			}
			result = new Result(taskId, code, (Exception)object);
		} else {
			result = new Result(taskId);
			result.putValue("value", object.toString());
		}
		ResultCache.getInstance().save(result);
	}

	private static <P> void startTracing(Executable<P, ?> service, P data) {
		if (TracingParam.class.isAssignableFrom(data.getClass())) {
			String traceId = ((TracingParam)data).getTraceId();
			String rpcId = ((TracingParam)data).getRpcId();
			String serviceName = service.getClass().getSimpleName();
			Invoker invoker = new Invoker("/listener", serviceName, "execute");
			TraceContext.initiate(rpcId, invoker, traceId);
			TracingCollector.setT1(null);
		}
	}
	private static void finishTracing(boolean succ, Exception ex) {
		if (!succ) {
			TracingCollector.markError(ex);
		}
		TracingCollector.setT2();
//      TraceContext.clear();
	}
}
