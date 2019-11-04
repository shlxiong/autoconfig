package com.openxsl.config.rocketmq.test;

import org.apache.rocketmq.common.message.MessageExt;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.rocketmq.ListenerMeta;
import com.openxsl.config.rocketmq.core.RocketListener;

@ScanConfig
@ListenerMeta(group="MYSQL92", topic="TopicTest", selector="TAGS in ('TagA', 'TagB') and (var1 is not null and var1 between 0 and 3)")
public class FilterbySQLMessageListener implements RocketListener<MessageExt>{

	@Override
	public void onMessage(MessageExt message) {
		String body = new String(message.getBody());
		String tags = message.getTags();
		String property = message.getUserProperty("var1");
		logger.info("Receive a message: {}, tag:{}, var1:{}", body, tags, property);		
	}

}
