package com.openxsl.config.filter.invoke;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

public class ServiceMethodInvocation implements MethodInvocation {
	private String serviceName;
	private String method;
	private Object[] args;
	
	public ServiceMethodInvocation(String serviceName, String method, Object...args) {
		this.serviceName = serviceName;
		this.method = method;
		this.args = args;
	}

	@Override
	public Object[] getArguments() {
		return args;
	}

	@Override
	public Object proceed() throws Throwable {
		return null;
	}

	@Override
	public Object getThis() {
		return serviceName;
	}

	@Override
	public AccessibleObject getStaticPart() {
		return null;
	}

	@Override
	public Method getMethod() {
		return null;
	}
	
	public String getMethodName() {
		return this.method;
	}

}
