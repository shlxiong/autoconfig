package com.openxsl.config.tracing.rocket;

import java.util.List;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import com.openxsl.config.loader.GraceServiceLoader;

/**
 * RocketMQ MessageHook注册类
 * 
 * @author xiongsl
 */
public class RocketHookRegister {
	private static final String HOOK_CLASS = 
			"com.openxsl.tracing.filter.TraceRocketHook";
	
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
