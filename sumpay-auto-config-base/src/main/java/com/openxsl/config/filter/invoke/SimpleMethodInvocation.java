package com.openxsl.config.filter.invoke;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.Assert;

import com.openxsl.config.util.BeanUtils;

public class SimpleMethodInvocation implements MethodInvocation {
	private Method method;
	private Object[] arguments;
	private Object targetObject;
	private Object result;
	
	public SimpleMethodInvocation(Object bean) {
		Assert.notNull(bean, "The Invocation-Bean is null");
		this.targetObject = bean;
	}
	
	@Override
	public Object[] getArguments() {
		return arguments;
	}

	@Override
	public Object proceed() throws Throwable {
		if (result != null) {
			return result;
		} else if (method != null) {
			return method.invoke(targetObject, arguments);
		} else {
			return null;
		}
	}

	@Override
	public Object getThis() {
		return targetObject;
	}

	@Override
	public AccessibleObject getStaticPart() {
		return null;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	public Object getTargetObject() {
		return targetObject;
	}

	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public void setMethod(String method, Object[] arguments) {
		this.method = BeanUtils.findBestMethod(targetObject.getClass(), method, arguments);
		this.arguments = arguments;
	}

}
