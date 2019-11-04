package com.openxsl.config.rocketmq.test;

import org.apache.rocketmq.common.message.MessageExt;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.rocketmq.ListenerMeta;
import com.openxsl.config.rocketmq.core.RocketListener;

@ScanConfig
@ListenerMeta(group="FILTERSOURCE", topic="TopicFilter", selector="filter=com.openxsl.config.rocketmq.DemoMessageFilter")
public class FilterbySourceMessageListener implements RocketListener<MessageExt> {

	@Override
	public void onMessage(MessageExt message) {
		String body = new String(message.getBody());
		String tags = message.getTags();
		String keys = message.getKeys();
		logger.info("Receive a message: {}, tag:{}, keys:{}", body, tags, keys);
	}

}
