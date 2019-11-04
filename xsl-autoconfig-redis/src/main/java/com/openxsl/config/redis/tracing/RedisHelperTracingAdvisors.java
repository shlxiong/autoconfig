package com.openxsl.config.redis.tracing;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.AntPathMatcher;

import com.openxsl.config.Environment;
import com.openxsl.config.redis.GenericRedisHelper;
import com.openxsl.config.redis.ListRedisHelper;
import com.openxsl.config.redis.MapRedisHelper;
import com.openxsl.config.util.BeanUtils;
import com.openxsl.config.util.StringUtils;

/**
 * 拦截RedisHelper的方法，set key
 * 
 * @author xiongsl
 */
public class RedisHelperTracingAdvisors {
	private static final ClassLoader CLASS_LOADER = BeanUtils.getClassLoader();
	private static final boolean DISABLED = !Environment.getProperty("spring.tracing.enable.redis",
					Boolean.class, false);
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static final <T> T getProxyBean(Object targetBean) {
		RedisTemplate<?,?> template = (RedisTemplate<?,?>)
				BeanUtils.getPrivateField(targetBean, "template");
		if (DISABLED || template instanceof Advised) {
			try {
				FlexibleRedisTemplate temp = (FlexibleRedisTemplate)
						((Advised)template).getTargetSource().getTarget();
				temp.setTracing(false);
			} catch (Exception e) {
				//e.printStackTrace();
			}
			return (T)targetBean;
		}
		
		String methods = "";
		if (targetBean instanceof GenericRedisHelper) {
			methods = "save*|delete*|get*|increase*|select";
		} else if (targetBean instanceof ListRedisHelper) {
			methods = "add*|remove*|get|set|save|clear|size";
		} else if (targetBean instanceof MapRedisHelper) {
			methods = "save*|put*|get|getAndSet|increase*|clear|size";
		}
		RedisHelperPointcut pointcut = new RedisHelperPointcut(methods);
		Advisor advisor = new DefaultPointcutAdvisor(pointcut, new RedisHelperAdvice());
		
    	ProxyFactory proxyFactory = new ProxyFactory(targetBean);
    	proxyFactory.addAdvisor(advisor);
    	ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyTargetClass(true);
        proxyFactory.copyFrom(proxyConfig);
        return (T)proxyFactory.getProxy(CLASS_LOADER);
    }
	
	private static class RedisHelperPointcut extends DynamicMethodMatcherPointcut{
		final AntPathMatcher matcher = new AntPathMatcher();
		private final String[] patterns;
		
		public RedisHelperPointcut(String patterns) {
			this.patterns = StringUtils.split(patterns, "\\|");
		}

		@Override
		public boolean matches(Method method, Class<?> targetClass, Object... args) {
			for (String pattern : patterns) {
				if (matcher.match(pattern, method.getName())) {
					return true;
				}
			}
			return false;
		}
		@Override
		public boolean matches(Method method, Class<?> targetClass) {
			return true;
		}
		
	}
	
	private static class RedisHelperAdvice implements MethodInterceptor {
		
		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			String methodName = invocation.getMethod().getName();
			Object[] args = invocation.getArguments();
			if (args[0] instanceof String) {
				String key = (String)args[0];
				RedisOptsContext.setKey(key);
				RedisOptsContext.setMethod(methodName);
			}
			return invocation.proceed();
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		RedisHelperPointcut pointcut = new RedisHelperPointcut("save*|put*|get|getAndSet|increase*|clear|size");
		
		Method method = MapRedisHelper.class.getDeclaredMethod("init");
		System.out.println(pointcut.matches(method, null));
	}
}
