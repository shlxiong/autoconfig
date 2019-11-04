package com.openxsl.config.thread.tracing;

import com.openxsl.config.filter.tracing.TraceContext;

/**
 * 追踪调用链的接口参数
 * @author xiongsl
 */
public class TracingParam {
	private transient String traceId;
	private transient String rpcId;

	public TracingParam() {  //Request Thread
		traceId = TraceContext.getTraceId();
		rpcId = TraceContext.newRpc(null);  //new rpcId
		TraceContext.popStack();
	}
	
	public String getTraceId() {
		return traceId;
	}
	public String getRpcId() {
		return rpcId;
	}

}
