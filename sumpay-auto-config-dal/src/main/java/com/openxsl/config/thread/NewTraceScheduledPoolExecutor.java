package com.openxsl.config.thread;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.filter.tracing.TracingCollector;
import com.openxsl.config.logger.context.LoggerContext;

/**
 * java.concurrent定时任务，每次都产生一个新的TraceID
 * 
 * <pre>
 * ScheduledThreadPoolExecutor.scheduleAtFixedRate(command, initialDelay, period, unit)
 *   ->this.delayedExecute(t);
 *       ->workQueue.add(task);
 *         addWorker(null, false);    //ThreadPoolExecutor
 *            ->new Worker(firstTask);  //extends runnable
 *                ->threadFactory.newThread(worker);  //this.thread-不会退出，除非shutdown
 *              worker.thread.start();
 *                ->worker.run(); =>runWorker(worker);
 *                    ->while (workQueue.take() != null); //死循环
 *                        ->beforeExecutor();
 *                          task.run();
 *                          afterExecutor();
 * </pre>
 * 
 * @author xiongsl
 */
public class NewTraceScheduledPoolExecutor extends ScheduledThreadPoolExecutor {
	private static final AtomicInteger THREAD_ID = new AtomicInteger(0);
	private final Invoker traceInvoker = new Invoker("sched", "ScheduledExecutorService", "schedule");

	public NewTraceScheduledPoolExecutor(String group, int corePoolSize) {
		super(corePoolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, group+"-"+THREAD_ID.incrementAndGet());
			}
		});
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		LoggerContext.clear();
		TraceContext.initiate(null, traceInvoker);
		TracingCollector.setT1(null);
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		TracingCollector.setT2();
	}

}
