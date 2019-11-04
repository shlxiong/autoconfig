package com.openxsl.config.rocketmq.test;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.rocketmq.ListenerMeta;
import com.openxsl.config.rocketmq.core.RocketListener;

@ScanConfig
@ListenerMeta(topic="topic_broadcast", group="TEST", messageModel=MessageModel.BROADCASTING)
public class BroadcastMessageListener implements RocketListener<MessageExt> {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onMessage(MessageExt message) {
		long delayTime = System.currentTimeMillis() - message.getBornTimestamp();
		long storage = System.currentTimeMillis() - message.getStoreTimestamp();
		String body = new String(message.getBody());
		
		logger.info("Receive a broadcast message: {}, delays: {}, duration: {}",
					body, delayTime, storage);
	}

}
