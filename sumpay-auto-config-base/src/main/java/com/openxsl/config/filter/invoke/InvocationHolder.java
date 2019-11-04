package com.openxsl.config.filter.invoke;

import org.aopalliance.intercept.MethodInvocation;

public class InvocationHolder {
	private static ThreadLocal<MethodInvocation> localMethodInvoc
				= new ThreadLocal<MethodInvocation>();
	
	public static MethodInvocation get() {
		return localMethodInvoc.get();
	}
	public static void set(MethodInvocation mi) {
		localMethodInvoc.set(mi);
	}
	
	public static void setResult(Object result) {
		if (get() == null) {
			throw new IllegalStateException("No MethodInvocation was found in ThreadLocal");
		}
		((SimpleMethodInvocation)get()).setResult(result);
	}
	
	public static MethodInvocation remove() {
		MethodInvocation methodInvoc = get();
		if (methodInvoc != null) {
			localMethodInvoc.remove();
		}
		return methodInvoc;
	}
	
	public static void setServiceInvoc(String service, String method, Object... args) {
		set( new ServiceMethodInvocation(service, method, args) );
	}
	public static void setSimpleInvoc(Object bean, String method, Object... args) {
		if (get() == null) {
			set(new SimpleMethodInvocation(bean));
		}
		((SimpleMethodInvocation)get()).setMethod(method, args);
	}
	
}
