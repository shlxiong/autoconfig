package com.openxsl.config.kafka;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;

import com.openxsl.config.autodetect.ScanConfig;

@ScanConfig
//@KafkaListener(topics="test-topic", group="testGroup", errorHandler = "defaultErrorHandler")
public class TestListener {
	
	@KafkaHandler
	public void onMessage(String message) {
//		for (String message : messages) {
			System.out.println("Received: "+message);
//		}
	}

}
