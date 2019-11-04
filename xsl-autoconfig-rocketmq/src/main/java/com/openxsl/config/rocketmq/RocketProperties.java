package com.openxsl.config.rocketmq;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.openxsl.config.autodetect.ScanConfig;

/**
 * @author xiongsl
 * @modify 2017-12-31 客户端使用 <code>@ListenerMeta</code>标注
 * @modify 2018-01-31 为了减少rocket server连接，判断使用场景是生产者或消费者
 */
@ScanConfig
public class RocketProperties {
	@Value("${rocketmq.nameServer}")
	private String namesrvAddr;        //服务器地址
	@Value("${rocketmq.instanceName:}")
	private String instanceName;       //实例名
	@Value("${rocketmq.vipChannel:false}")
	private boolean vipChannel;
	@Value("${rocketmq.side:}")
	private String side;               //'P':生产者, 'C':消费者
	
	@Value("${rocketmq.producer.group:}")
	private String producerGroup;      //发送者分组
	@Value("${rocketmq.producer.transaction.listener:}")
	private String transactionListener;   //事务监听器
	@Value("${rocketmq.producer.sendMsgTimeout:3000}")
	private int sendMsgTimeout;        //发送超时时间
	@Value("${rocketmq.producer.maxMessageSize:4096000}")
	private int maxMessageSize;        //消息体最大字节数
	@Value("${rocketmq.producer.compressMsgBodyOverHowmuch:1024000}")
	private int compressMsgBodyOverHowmuch;   //压缩
	@Value("${rocketmq.producer.retryTimesWhenSendFailed:2}")
	private int retryTimesWhenFailed;  //失败重发次数
	@Value("${rocketmq.producer.retryTimesWhenSendAsyncFailed:2}")
	private int retryTimesWhenAsyncFailed;
	@Value("${rocketmq.consumer.threadMax:64}")
	private int threadMax;
	@Value("${rocketmq.consumer.threadMin:4}")
	private int threadMin;
	
	public boolean isProcuders() {
		return StringUtils.isEmpty(side) || "P".equals(side);
	}
	public boolean isConsumers() {
		return StringUtils.isEmpty(side) || "C".equals(side);
	}
	
	public String getNamesrvAddr() {
		return namesrvAddr;
	}
	public void setNamesrvAddr(String namesrvAddr) {
		this.namesrvAddr = namesrvAddr;
	}
	public String getProducerGroup() {
		return producerGroup;
	}
	public void setProducerGroup(String producerGroup) {
		this.producerGroup = producerGroup;
	}
	public int getSendMsgTimeout() {
		return sendMsgTimeout;
	}
	public void setSendMsgTimeout(int sendMsgTimeout) {
		this.sendMsgTimeout = sendMsgTimeout;
	}
	public int getMaxMessageSize() {
		return maxMessageSize;
	}
	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}
	public int getCompressMsgBodyOverHowmuch() {
		return compressMsgBodyOverHowmuch;
	}
	public void setCompressMsgBodyOverHowmuch(int compressMsgBodyOverHowmuch) {
		this.compressMsgBodyOverHowmuch = compressMsgBodyOverHowmuch;
	}
	public int getRetryTimesWhenAsyncFailed() {
		return retryTimesWhenAsyncFailed;
	}
	public void setRetryTimesWhenAsyncFailed(int retryTimesWhenAsyncFailed) {
		this.retryTimesWhenAsyncFailed = retryTimesWhenAsyncFailed;
	}
	public int getThreadMax() {
		return threadMax;
	}
	public void setThreadMax(int threadMax) {
		this.threadMax = threadMax;
	}
	public int getThreadMin() {
		return threadMin;
	}
	public void setThreadMin(int threadMin) {
		this.threadMin = threadMin;
	}
	public int getRetryTimesWhenFailed() {
		return retryTimesWhenFailed;
	}
	public void setRetryTimesWhenFailed(int retryTimesWhenFailed) {
		this.retryTimesWhenFailed = retryTimesWhenFailed;
	}
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}
	public String getInstanceName() {
		if (instanceName == null || instanceName.trim().length() == 0) {
			StringBuilder buffer = new StringBuilder();
			for (String hostPort : namesrvAddr.replace('.', '_').split(";")) {
				buffer.append(hostPort.split(":")[0]);  //IP地址
			}
			instanceName = buffer.toString();
		}
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public boolean isVipChannel() {
		return vipChannel;
	}
	public void setVipChannel(boolean vipChannel) {
		this.vipChannel = vipChannel;
	}
	public String getTransactionListener() {
		return transactionListener;
	}
	public void setTransactionListener(String transactionListener) {
		this.transactionListener = transactionListener;
	}
	
}
