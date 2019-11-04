package com.openxsl.config.filter.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class InvocTrace implements Serializable {
	private String traceId;
	private String rpcId;
	private long t1;    //SR
	private long t2;    //SS
	private long t3;    //CS
	private long t4;    //CR
	private String memo = "";
	private Object parameters;
	private boolean hasErrors;
	
	public InvocTrace(String rpcId, String traceId) {
		this.setRpcId(rpcId);
		this.setTraceId(traceId);
	}
	
	@Override
	public String toString() {
		return new StringBuilder("rpcId=").append(rpcId)
				.append(", T1=").append(t1).append(", T3=").append(t3)
				.append(", T4=").append(t4).append(", T2=").append(t2)
				.append(", traceId=").append(traceId)
				.append(", memo=").append(memo)
				.append(", hasErrors=").append(hasErrors)
				.toString();
	}
	
	public String getTraceId() {
		return traceId;
	}
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}
	public String getRpcId() {
		return rpcId;
	}
	public void setRpcId(String rpcId) {
		this.rpcId = rpcId;
	}
	public long getT1() {
		return t1;
	}
	public void setT1(long t1) {
		this.t1 = t1;
	}
	public long getT2() {
		return t2;
	}
	public void setT2(long t2) {
		this.t2 = t2;
	}
	public long getT3() {
		return t3;
	}
	public void setT3(long t3) {
		this.t3 = t3;
	}
	public long getT4() {
		return t4;
	}
	public void setT4(long t4) {
		this.t4 = t4;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public boolean isHasErrors() {
		return hasErrors;
	}
	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}
	public Object getParameters() {
		return parameters;
	}
	public void setParameters(Object parameters) {
		this.parameters = parameters;
	}

}
