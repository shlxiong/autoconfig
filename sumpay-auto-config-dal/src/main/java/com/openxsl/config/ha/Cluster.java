package com.openxsl.config.ha;

/**
 * 只发送一次
 * failover: 重试其他
 * failfast: 失败即抛错
 * failback: 失败定时重发
 * 发送多次
 * forking: （幂等性服务）并行调用多个服务器，只要一个成功即返回。
 * broadcast: 全部成功
 * @author xiongsl
 */
public enum Cluster {
	/** 重试其他 */
	FAILOVER,
	/** 失败即抛错 */
	FAILFAST,
	/** 失败定时重发 */
	FAILBACK,
	
	/** 全部成功 */
	BROADCAST,
	/** 并行调用多个服务器，只要一个成功即返回 */
	FORKING;
	
}
