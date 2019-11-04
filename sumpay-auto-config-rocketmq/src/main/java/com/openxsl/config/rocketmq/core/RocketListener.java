package com.openxsl.config.rocketmq.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务监听类
 * @author xiongsl
 */
public interface RocketListener<T> {
	final Logger logger = LoggerFactory.getLogger(RocketListener.class); 

	/**
	 * 处理消息
	 * @param message 消息体或MessageExt
	 */
	public void onMessage(T message);
	
}
