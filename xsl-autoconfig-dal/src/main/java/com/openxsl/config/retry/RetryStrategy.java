package com.openxsl.config.retry;

public class RetryStrategy {
	/**
	 * 总共两次，间隔一次为3
	 */
	public static RetryStrategy DEFAULT = new RetryStrategy(2, 2000, 1000);
	public static RetryStrategy NORETRY = new RetryStrategy(1, 0, 0);
	
	private int retries;        //重试次数
	private int interval;       //间隔时间
	private int increament;     //逐次增加时间
	private int timeout;        //总超时时间
	
	public RetryStrategy(){}
	
	public RetryStrategy(int retries, int interval, int increament){
		this.setRetries(retries);
		this.setInterval(interval);
		this.setIncreament(increament);
	}
	
	/**
	 * 第 i次间隔时间，(a + b*2^i)
	 */
	public int getSleeptime(int i){
		return interval + (1<<i) * increament;
	}
	
	public int getRetries() {
		return retries;
	}
	public void setRetries(int retries) {
		this.retries = retries;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public int getIncreament() {
		return increament;
	}
	public void setIncreament(int increament) {
		this.increament = increament;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
}
