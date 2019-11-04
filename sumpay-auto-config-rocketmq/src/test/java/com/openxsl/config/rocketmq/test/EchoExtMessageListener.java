package com.openxsl.config.rocketmq.test;

import org.apache.rocketmq.common.message.MessageExt;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.rocketmq.ListenerMeta;
import com.openxsl.config.rocketmq.core.RocketListener;

@ScanConfig
@ListenerMeta(topic="topic_xiong2", group="OTHER", selector="key_A || key_C")
public class EchoExtMessageListener implements RocketListener<MessageExt> {

	@Override
	public void onMessage(MessageExt message) {
		String body = new String(message.getBody());
		String header = message.getProperty("user.directory");
		logger.info("Receive a message: {}, header is: {}", body, header);
	}
	
	public static void main(String[] args) {
		Class<?> messageType = MessageExt.class;
		boolean flag = MessageExt.class.isAssignableFrom(messageType);
		System.out.println(flag);
	}

}
