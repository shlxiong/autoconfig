package com.openxsl.config.kafka.core;

import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;

import com.openxsl.config.autodetect.ScanConfig;

/**
 * Kafka消费异常处理类（@KafkaListener(errorHandler="")）
 * 
 * @author xiongsl
 */
@ScanConfig("defaultErrorHandler")
public class ConsumerErrorHandler implements KafkaListenerErrorHandler {
	
	@Override
	public Object handleError(Message<?> message, ListenerExecutionFailedException exception) throws Exception {
		System.err.println("fail=====" + exception);
		return null;
	}

}
