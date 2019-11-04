package com.openxsl.config.thread.tracing;

import java.util.concurrent.Callable;

import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.filter.tracing.TracingCollector;

public class TraceableCallable<T> extends TracingParam implements Callable<T>{
	private final Invoker invoker;
	private final Callable<T> worker;
	
	public TraceableCallable(Callable<T> runnable, Invoker... traceInvoker) {
		super();
		Invoker invoker2 = (traceInvoker.length > 0) ? traceInvoker[0] : null;
		this.invoker = (invoker2 != null) ? invoker2
				: new Invoker("tpool", "TraceableCallable", "call");
		worker = runnable;
	}

	@Override
	public T call() throws Exception{
		TraceContext.initiate(this.getRpcId(), invoker, this.getTraceId());
		TracingCollector.setT1(null);
		try {
			return worker.call();
		} finally {
			TracingCollector.setT2();
		}
	}

}
