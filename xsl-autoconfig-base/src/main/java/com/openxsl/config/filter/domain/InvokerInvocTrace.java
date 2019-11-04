package com.openxsl.config.filter.domain;

@SuppressWarnings("serial")
public class InvokerInvocTrace extends InvocTrace {
	private Invoker invoker;
	
	public InvokerInvocTrace(String rpcId, String traceId) {
		super(rpcId, traceId);
	}
	public InvokerInvocTrace(String rpcId, String traceId, Invoker invoker) {
		super(rpcId, traceId);
		this.setInvoker(invoker);
	}
	
	@Override
	public String toString() {
		return new StringBuilder(super.toString())
				.append(", invoker=(").append(invoker).append(")")
				.toString();
	}

	public Invoker getInvoker() {
		return invoker;
	}

	public void setInvoker(Invoker invoker) {
		this.invoker = invoker;
	}

}
