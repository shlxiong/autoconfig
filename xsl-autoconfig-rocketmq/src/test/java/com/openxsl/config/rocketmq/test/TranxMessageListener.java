package com.openxsl.config.rocketmq.test;

import org.apache.rocketmq.common.message.MessageExt;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.rocketmq.ListenerMeta;
import com.openxsl.config.rocketmq.core.RocketListener;

@ScanConfig
@ListenerMeta(group="TRANSX", topic="topic_tranx")
public class TranxMessageListener implements RocketListener<MessageExt> {

	@Override
	public void onMessage(MessageExt message) {
		String body = new String(message.getBody());
		logger.info("Receive a message: {}", body);
	}

}
