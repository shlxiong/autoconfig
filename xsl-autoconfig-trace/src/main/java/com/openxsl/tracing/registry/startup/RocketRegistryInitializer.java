package com.openxsl.tracing.registry.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.openxsl.config.rocketmq.core.RocketListenerContainer;
import com.openxsl.config.tracing.service.RegistryService;
import com.openxsl.config.tracing.service.protocol.MsgRegistry;
import com.openxsl.tracing.registry.model.MessageTopic;

/**
 * RocketMQ资源注册器
 *  /rocketmq/namesrv:topic/producer
 * @author xiongsl
 */
public class RocketRegistryInitializer extends AbstractRegistryInitializer<RocketListenerContainer> {

	@Override
	public void onStartup(ServletContext sc) throws ServletException {
		System.out.println("Rocketmq-TracingInitializer start, order=530");
		super.onStartup(sc);
	}

	@Override
	protected String getThreadName() {
		return "RocketMQ-Registry";
	}

	@Override
	protected Class<? extends RegistryService> getServiceClass() {
		return MsgRegistry.class;
	}

	@Override
	protected void register(RocketListenerContainer contextBean) {
		String namesrv = contextBean.getNameServer();
		String topic = contextBean.getTopic();
		String tags = contextBean.getSelector();
		service.registerServer(new MessageTopic(namesrv, topic, tags));
	}

}
