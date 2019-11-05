package com.openxsl.tracing.registry.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.openxsl.config.redis.RedisProperties;
import com.openxsl.config.tracing.service.RegistryService;
import com.openxsl.config.tracing.service.protocol.RedisRegistry;
import com.openxsl.tracing.registry.model.RedisRegInfo;

public class RedisRegistryInitializer extends AbstractRegistryInitializer<RedisProperties> {

	@Override
	public void onStartup(ServletContext sc) throws ServletException {
		System.out.println("Redis-TracingInitializer start, order=520");
		super.onStartup(sc);
	}

	@Override
	protected String getThreadName() {
		return "Redis-Registry";
	}

	@Override
	protected Class<? extends RegistryService> getServiceClass() {
		return RedisRegistry.class;
	}

	@Override
	protected void register(RedisProperties contextBean) {
		String redisUrl = contextBean.getRegistryUrl();
		service.registerClient(new RedisRegInfo(redisUrl));
	}

}
