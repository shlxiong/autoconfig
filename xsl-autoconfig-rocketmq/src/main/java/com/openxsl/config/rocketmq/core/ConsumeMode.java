package com.openxsl.config.rocketmq.core;

/**
 * 消费模式： 并发或顺序
 * @author xiongsl
 */
public enum ConsumeMode {
	/**
	 * 并发
	 */
	CONCURRENTLY, 
	/**
	 * 严格顺序
	 */
	ORDERLY;

}
