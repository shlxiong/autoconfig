package com.openxsl.config.thread.tracing;

import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.filter.tracing.TracingCollector;

public class TraceableRunnable extends TracingParam implements Runnable{
	private final Invoker invoker;
	private final Runnable worker;
	
	public TraceableRunnable(Runnable runnable, Invoker... traceInvoker) {
		super();
		Invoker invoker2 = (traceInvoker.length > 0) ? traceInvoker[0] : null;
		this.invoker = (invoker2 != null) ? invoker2
				: new Invoker("tpool", "TraceableRunnable", "run");
		worker = runnable;
	}

	@Override
	public void run() {
		TraceContext.initiate(this.getRpcId(), invoker, this.getTraceId());
		TracingCollector.setT1(null);
		try {
			worker.run();
		} finally {
			TracingCollector.setT2();
		}
	}

}
