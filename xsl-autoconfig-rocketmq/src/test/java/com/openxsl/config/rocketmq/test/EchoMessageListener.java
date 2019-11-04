package com.openxsl.config.rocketmq.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.rocketmq.ListenerMeta;
import com.openxsl.config.rocketmq.core.RocketListener;

@ScanConfig
@ListenerMeta(topic="topic_xiongsl", group="NORMAL")
public class EchoMessageListener implements RocketListener<String>{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onMessage(String message) {
		logger.info("Receive a message: {}", message);
	}
	
}
