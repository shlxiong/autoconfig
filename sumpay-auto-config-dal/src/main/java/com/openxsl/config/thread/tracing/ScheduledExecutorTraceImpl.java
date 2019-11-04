package com.openxsl.config.thread.tracing;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.openxsl.config.thread.NewTraceScheduledPoolExecutor;

public class ScheduledExecutorTraceImpl extends ExecutorServiceTraceImpl implements ScheduledExecutorService {
	private final ScheduledExecutorService scheduledService;
	private final NewTraceScheduledPoolExecutor newTraceService;
	
	public ScheduledExecutorTraceImpl(ScheduledExecutorService executorService) {
		super(executorService);
		this.scheduledService = executorService;
		int coreSize = 1;
		if (executorService instanceof ScheduledThreadPoolExecutor) {
			coreSize = ((ScheduledThreadPoolExecutor)executorService).getCorePoolSize();
		}
		newTraceService = new NewTraceScheduledPoolExecutor("ScheduledExecutor", coreSize);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return scheduledService.schedule(new TraceableRunnable(command), delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return scheduledService.schedule(new TraceableCallable<V>(callable), delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return newTraceService.scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return newTraceService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

}
