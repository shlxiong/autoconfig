package com.openxsl.config.filter;

import org.aopalliance.intercept.MethodInvocation;

public interface TracingFilter {
	
	/**
	 * 方法调用前的操作
	 * @param mi SimpleMethodInvocation
	 */
	public void before(MethodInvocation mi);
	
	public void after(MethodInvocation mi, Object result);
	
	public String getType();
	
}
