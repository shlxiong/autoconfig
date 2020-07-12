package com.openxsl.config.thread;

import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.filter.tracing.TracingCollector;
import com.openxsl.config.logger.context.LoggerContext;
import com.openxsl.config.thread.tracing.ExecutorServiceTraceImpl;
import com.openxsl.config.thread.tracing.TraceableCallable;
import com.openxsl.config.thread.tracing.TraceableRunnable;
import com.openxsl.config.thread.tracing.TraceableTimerTask;

/**
 * 分组创建线程的工厂类
 * 
 * @author xiongsl
 */
public class GrouppedThreadFactory implements ThreadFactory {
	private static final Logger logger = LoggerFactory.getLogger(GrouppedThreadFactory.class);
	
	private final String group;
	private final AtomicInteger serialNo = new AtomicInteger(1);
	private final ScheduledExecutorService scheduler;
	private final ExecutorService executor;
	private final int coreSize = Runtime.getRuntime().availableProcessors();
	private final int queueSize = 10 * 1024;
	
	public GrouppedThreadFactory(String group){
		this.group = group;
		this.scheduler = TtlExecutors.getTtlScheduledExecutorService(
								new ScheduledThreadPoolExecutor(1, this));  //Executors.newScheduledThreadPool
		executor = this.newThreadPool(Math.min(coreSize, 8), 10, 60);    //最多10个线程
	}
	private String getName() {
		return group+"-"+serialNo.getAndIncrement();
	}
	
	/**
	 * ThreadFactory创建一个线程，在ThreadPoolExecutor中run方法是个死循环
	 * @see #newThread("group-1", false, Runnable)
	 */
	@Override
	public Thread newThread(Runnable runner) {
		return new MyThread(TtlRunnable.get(runner), this.getName());
	}
	
	/**
	 * 创建一个线程， 带调用链功能
	 * @see #newThread("group-1", boolean, Runnable)
	 */
	public Thread newThread(Runnable runner, boolean daemon) {
		return newThread(this.getName(), daemon, runner);
	}
	/**
	 * 新建一个线程， 带调用链功能
	 * @param fullName 线程名
	 * @param daemon 是否守护线程
	 * @param runner 执行类
	 */
	public static Thread newThread(String fullName, boolean daemon, Runnable runner) {
		Runnable worker = TtlRunnable.get(runner);
		if (!TraceContext.isDisabled()) {
			Invoker invoker = new Invoker("tpool", fullName, "run");
			worker = new TraceableRunnable(worker, invoker);
		}
		Thread t = new MyThread(worker, fullName);
        t.setDaemon(daemon);
        return t;
	}
	
	/**
	 * @see #newThreadPool(int, int, int, 1024) - TTL线程池
	 */
	public ExecutorService newThreadPool(int coreSize, int maxSize, int keepAlive) {
		return newThreadPool(coreSize, maxSize, keepAlive, queueSize);
	}
	/**
	 * 新建一个线程池(TTL)
	 * @param coreSize 最小线程数
	 * @param maxSize  最大线程数
	 * @param keepAlive 线程存活时间（秒）
	 * @param queueSize 缓存队列大小
	 */
	public ExecutorService newThreadPool(int coreSize, int maxSize, int keepAlive, int queueSize,
										RejectedExecutionHandler... rejects) {
		if (!TraceContext.isDisabled() && TraceContext.isOver()) {
			TraceContext.initiate(null, null);
		}
		ExecutorService executor = TtlExecutors.getTtlExecutorService(
						new ThreadPoolExecutor(coreSize, maxSize, keepAlive, TimeUnit.SECONDS,
								new LinkedBlockingQueue<Runnable>(queueSize), this,
								rejects.length>0?rejects[0]:new ThreadPoolExecutor.AbortPolicy())
				);
		if (!TraceContext.isDisabled()) {
			executor = new ExecutorServiceTraceImpl(executor);
		}
		return executor;
	}
	/**
	 * 新建一个线程池，当池满了新建线程来处理
	 * @param coreSize 最小线程数
	 * @param maxSize  最大线程数
	 * @param keepAlive 线程存活时间（秒）
	 */
	public ExecutorService newScalableThreadPool(int coreSize, int maxSize, int keepAlive) {
		return newThreadPool(coreSize, maxSize, keepAlive, queueSize, new NewThreadRunsPolicy());
	}
	/**
	 * 新建一个用于调度的线程池
	 * @param coreSize
	 */
	public ScheduledExecutorService newScheduledPool(int coreSize) {
		return new NewTraceScheduledPoolExecutor(group, coreSize);
	}
	
	/**
	 * 执行Runnable
	 */
	public void execute(Runnable runner, ExecutorService... executors) {
		if (executors.length>0 && executors[0]!=null) {
			executors[0].submit(new TraceableRunnable(runner));
		} else {
			executor.submit(new TraceableRunnable(runner));
		}
	}
	public void execute(Runnable runner, Invoker traceInvoker, ExecutorService... executors) {
		if (executors.length>0 && executors[0]!=null) {
			executors[0].submit(new TraceableRunnable(runner, traceInvoker));
		} else {
			executor.submit(new TraceableRunnable(runner, traceInvoker));
		}
	}
//	public <T> List<Future<T>> invokerAll(Collection<Callable<T>> tasks, long timeout, TimeUnit unit,
//					ExecutorService... executors) {
//		try {
//			ExecutorService executor = (executors.length>0 && executors[0]!=null) ? executors[0]
//									: this.executor;
//			if (timeout <= 0) {
//				return executor.invokeAll(tasks);
//			} else {
//				return executor.invokeAll(tasks, timeout, unit);
//			}
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
//	}
	//在单独的Trace上下文中执行（调度）
	public void executeAlone(Runnable runner, Invoker traceInvoker, ExecutorService... executors) {
		Runnable worker = new Runnable() {
			@Override
			public void run() {
				LoggerContext.clear();
				TraceContext.initiate(null, traceInvoker);
				TracingCollector.setT1(null);
				try {
					runner.run();
				} finally {
					TracingCollector.setT2();
				}
			}
		};
		if (executors.length>0 && executors[0]!=null) {
			executors[0].submit(worker);
		} else {
			executor.submit(worker);
		}
	}
	/**
	 * 调用Callable
	 */
	public <T> Future<T> submit(Callable<T> callable, ExecutorService... executors) {
		if (executors.length>0 && executors[0]!=null) {
			return executors[0].submit(new TraceableCallable<T>(callable));
		} else {
			return executor.submit(new TraceableCallable<T>(callable));
		}
	}
	/**
	 * 调用Callable，指定
	 */
	public <T> Future<T> submit(Callable<T> callable, Invoker traceInvoker, ExecutorService... executors) {
		if (executors.length>0 && executors[0]!=null) {
			return executors[0].submit(new TraceableCallable<T>(callable, traceInvoker));
		} else {
			return executor.submit(new TraceableCallable<T>(callable, traceInvoker));
		}
	}
	public <T> Future<T> submitCompletion(Callable<T> callable,
					ExecutorCompletionService<T> completionService){
		return completionService.submit(new TraceableCallable<T>(callable));
	}
	/**
	 * 延迟调用任务（仅一次）
	 */
	public void schedule(TimerTask task, long delaySec,
						ScheduledExecutorService... schedulers) {
		TraceableTimerTask traceable;
		if (task instanceof TraceableTimerTask) {
			traceable = (TraceableTimerTask)task;
		} else {
			traceable = new TraceableTimerTask(task);
		}
		if (schedulers.length>0 && schedulers[0]!=null) {
			schedulers[0].schedule(traceable, delaySec, TimeUnit.SECONDS);
		} else {
			scheduler.schedule(traceable, delaySec, TimeUnit.SECONDS);
		}
	}
	/**
	 * 周期性调用任务（循环，不推荐）
	 */
	public void scheduleAtFixedRate(TimerTask task, long delaySec, long period,
						ScheduledExecutorService... schedulers) {
		TraceableTimerTask traceable;
		if (task instanceof TraceableTimerTask) {
			traceable = (TraceableTimerTask)task;
		} else {
			traceable = new TraceableTimerTask(task);
		}
		if (schedulers.length>0 && schedulers[0]!=null) {
			schedulers[0].scheduleAtFixedRate(traceable, delaySec, period, TimeUnit.SECONDS);
		} else {
			scheduler.scheduleAtFixedRate(traceable, delaySec, period, TimeUnit.SECONDS);
		}
	}
	
	
	//不要显示地创建线程
	static class MyThread extends Thread{
//		private TracingParam trace;
		
		MyThread(Runnable runner, String name){
			super(runner, name);
//			trace = new TracingParam();
		}
		
//		@Override
//		public void run() {
//			Invoker invoker = new Invoker("tpool", getName(), "run");
//			TraceContext.initiate(trace.getRpcId(), invoker, trace.getTraceId());
//			TracingCollector.setT1(null);
//			try {
//				super.run();
//			}finally {
//				TracingCollector.setT2();
//			}
//		}
		
		@Override
		public void setUncaughtExceptionHandler(final UncaughtExceptionHandler handler) {
			super.setUncaughtExceptionHandler( new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable exp) {
					logger.error("Catch unkown-exception: ", exp);
					handler.uncaughtException(t, exp);
				}
			});
		}
		
	}

}
