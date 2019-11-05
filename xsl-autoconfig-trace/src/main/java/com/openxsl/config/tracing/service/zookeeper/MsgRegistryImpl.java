package com.openxsl.config.tracing.service.zookeeper;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.rocketmq.core.RocketListenerContainer;
import com.openxsl.config.tracing.service.protocol.MsgRegistry;
import com.openxsl.tracing.registry.model.MessageTopic;
import com.openxsl.tracing.registry.model.Registration;

@ScanConfig
public class MsgRegistryImpl extends BaseZkRegistry implements MsgRegistry{

	@Override
	protected String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public Registration convertRegistra(Object bean) {
		RocketListenerContainer listener = (RocketListenerContainer)bean;
		String namesrv = listener.getNameServer();
		String topic = listener.getTopic();
		String tags = listener.getSelector();
		return new MessageTopic(namesrv, topic, tags);
	}

	@Override
	public Class<?> getContextBeanType() {
		return RocketListenerContainer.class;
	}

}
