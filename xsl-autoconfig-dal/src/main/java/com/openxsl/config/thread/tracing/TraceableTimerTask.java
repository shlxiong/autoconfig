package com.openxsl.config.thread.tracing;

import java.util.TimerTask;

import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.filter.tracing.TracingCollector;

public class TraceableTimerTask extends TimerTask{
	private final TracingParam trace;
	private final Invoker invoker;
	private final TimerTask task;
	
	public TraceableTimerTask(TimerTask task, Invoker... traceInvoker) {
		this.trace = new TracingParam();
		Invoker invoker2 = (traceInvoker.length > 0) ? traceInvoker[0] : null;
		this.invoker = (invoker2 != null) ? invoker2
				: new Invoker("tpool", "TraceableTimerTask", "run");
		this.task = task;
	}

	@Override
	public void run() {
		TraceContext.initiate(trace.getRpcId(), invoker, trace.getTraceId());
		TracingCollector.setT1(null);
		try {
			task.run();
		} finally {
			TracingCollector.setT2();
		}
	}

}
