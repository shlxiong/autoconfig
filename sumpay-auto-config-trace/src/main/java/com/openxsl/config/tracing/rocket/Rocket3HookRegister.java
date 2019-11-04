package com.openxsl.config.tracing.rocket;

import java.util.List;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.hook.ConsumeMessageHook;
import com.alibaba.rocketmq.client.hook.SendMessageHook;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;

import com.openxsl.config.loader.GraceServiceLoader;

public class Rocket3HookRegister {
	private static final String HOOK_CLASS = 
			"com.openxsl.config.tracing.filter.TraceRocket3Hook";
	
	public static void registerTo(DefaultMQProducer producer) {
		List<SendMessageHook> traceHooks = 
				GraceServiceLoader.loadServices(SendMessageHook.class, HOOK_CLASS);
		for (SendMessageHook hook : traceHooks) {
			producer.getDefaultMQProducerImpl().registerSendMessageHook(hook);
		}
	}
	
	public static void registerTo(DefaultMQPushConsumer consumer) {
		List<ConsumeMessageHook> traceHooks = 
				GraceServiceLoader.loadServices(ConsumeMessageHook.class, HOOK_CLASS);
		for (ConsumeMessageHook hook : traceHooks) {
			consumer.getDefaultMQPushConsumerImpl().registerConsumeMessageHook(hook);
		}
	}

}
