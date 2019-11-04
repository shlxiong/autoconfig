package com.openxsl.config.kafka;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;

import com.alibaba.fastjson.JSON;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.filter.domain.InvocTrace;

@ScanConfig
@KafkaListener(topics="TracingTopic", group="tracing", errorHandler = "defaultErrorHandler")
public class KafkaTracingListener {
	
	@KafkaHandler
	public void echoInvocTrace(byte[] content) {
		for (InvocTrace trace : JSON.parseArray(new String(content), InvocTrace.class)){
			System.out.println(trace);
		}
	}

}
