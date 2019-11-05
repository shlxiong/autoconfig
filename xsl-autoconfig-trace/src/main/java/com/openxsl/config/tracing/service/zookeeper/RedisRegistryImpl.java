package com.openxsl.config.tracing.service.zookeeper;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.redis.RedisProperties;
import com.openxsl.config.tracing.service.protocol.RedisRegistry;
import com.openxsl.tracing.registry.model.RedisRegInfo;
import com.openxsl.tracing.registry.model.Registration;

@ScanConfig
public class RedisRegistryImpl extends BaseZkRegistry implements RedisRegistry {

	@Override
	protected String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public Registration convertRegistra(Object bean) {
		String redisUrl = ((RedisProperties)bean).getRegistryUrl();
		return new RedisRegInfo(redisUrl);
	}

	@Override
	public Class<?> getContextBeanType() {
		return RedisProperties.class;
	}
	
}
