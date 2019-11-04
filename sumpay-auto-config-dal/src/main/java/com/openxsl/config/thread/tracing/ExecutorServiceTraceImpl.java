package com.openxsl.config.thread.tracing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 适用调用链的线程池
 * 
 * @author xiongsl
 */
public class ExecutorServiceTraceImpl implements ExecutorService {
	private final ExecutorService executorService;
	
	public ExecutorServiceTraceImpl(ExecutorService executorService) {
        this.executorService = executorService;
    }

	@Override
	public void execute(Runnable command) {
		executorService.execute(new TraceableRunnable(command));
	}
	
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return executorService.submit(new TraceableCallable<T>(task));
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return executorService.submit(new TraceableRunnable(task), result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return executorService.submit(new TraceableRunnable(task));
	}

	@Override
	public void shutdown() {
		executorService.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return executorService.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return executorService.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executorService.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executorService.awaitTermination(timeout, unit);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		Collection<Callable<T>> traceables = new ArrayList<Callable<T>>(tasks.size());
		for (Callable<T> task : tasks) {
			traceables.add(task);
		}
		return executorService.invokeAll(traceables);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		Collection<Callable<T>> traceables = new ArrayList<Callable<T>>(tasks.size());
		for (Callable<T> task : tasks) {
			traceables.add(task);
		}
		return executorService.invokeAll(traceables, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		Collection<Callable<T>> traceables = new ArrayList<Callable<T>>(tasks.size());
		for (Callable<T> task : tasks) {
			traceables.add(task);
		}
		return executorService.invokeAny(traceables);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
		Collection<Callable<T>> traceables = new ArrayList<Callable<T>>(tasks.size());
		for (Callable<T> task : tasks) {
			traceables.add(task);
		}
		return executorService.invokeAny(traceables, timeout, unit);
	}

}
