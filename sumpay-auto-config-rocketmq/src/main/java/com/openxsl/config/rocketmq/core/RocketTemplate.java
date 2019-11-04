package com.openxsl.config.rocketmq.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionExecuter;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 发送消息的类，消息体是JSON字符串，消息头包括但不限于：TAG,KEYS,WAIT,DELAY
 * 
 * @author xiongsl
 */
public class RocketTemplate {//extends AbstractMessageSendingTemplate<String> {
	private static final Logger logger = LoggerFactory.getLogger(RocketTemplate.class);
	@Autowired
	private DefaultMQProducer producer;
	@Autowired(required=false)
	private ObjectMapper objectMapper;
	
	public void setProducer(DefaultMQProducer producer) {
		this.producer = producer;
	}
	public DefaultMQProducer getProducer() {
		return producer;
	}
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	/**
	 * 同步发送消息，等待Rocketmq响应后，再返回MessageID
	 * @param topic    TOPIC
	 * @param content  消息体
	 * @param headers  消息头(TAG,KEYS,WAIT,DELAY及其他)，发送完后会清空
	 * @throws Exception
	 */
	public String send(String topic, Object content, Map<String,Object> headers) throws Exception {
		//MessageHeaders msgHeader = new MessageHeaders(headers);
		Message message = this.toMessage(topic, content, headers);
		return this.doSend(message, -1);
	}
	/**
	 * 同步发送消息，等待Rocketmq响应后，再返回MessageID
	 * @param topic    TOPIC
	 * @param content  消息体
	 * @param headers  消息头(TAG,KEYS,WAIT,DELAY及其他)，发送完后会清空
	 * @throws Exception
	 */
	public String send(String topic, Object content, Map<String,Object> headers, 
					long timeout) throws Exception {
		Message message = this.toMessage(topic, content, headers);
		return this.doSend(message, timeout);
	}
	/**
	 * 发送事务性消息，Rocketmq只提供了Producer的本地事务，并不保证消费成功
	 * @param topic   TOPIC
	 * @param content 消息体
	 * @param headers 消息头(TAG,KEYS,WAIT,DELAY及其他)，发送完后会清空
	 * @param txExecutor 事务决断者，决定是提交还是回滚，只有COMMIT的消息才会被消费端接收
	 */
	public String sendTransactional(String topic, Object content, Map<String,Object> headers,
						LocalTransactionExecuter txExecutor) throws Exception {
		//在DefaultMQProducerImpl中，发送大致分三个步骤：
		//SendResult sendResult = this.send(msg);
		//tranExecuter.executeLocalTransactionBranch(msg, arg);
		//this.endTransaction(sendResult, localTransactionState, localException);
		
		TransactionMQProducer producer = (TransactionMQProducer)this.producer;
		Message message = this.toMessage(topic, content, headers);
		TransactionSendResult result = producer.sendMessageInTransaction(message, txExecutor, null);
		String tags = message.getTags();
		printResult(topic, tags, result, null);
		return result.getMsgId();
	}
	/**
	 * 向多个topic同时发送一条消息
	 * @param topics  TOPIC
	 * @param content 消息体
	 * @param headers 消息头(TAG,KEYS,WAIT,DELAY及其他)，发送完后会清空
	 * @throws IOException 
	 */
	public void sendTopics(String[] topics, Object content, Map<String,Object> headers) throws Exception {
		Message message = this.toMessage("NULL", content, headers);
		for (String topic : topics) {
			message.setTopic(topic);
			this.doSend(message, -1);
		}
	}
	/**
	 * 批量发送消息，要求所有的消息：topic相同且不能"%RETRY%"，isWaitStoreMsgOK相同，并且body总大小符合要求
	 * @param topic     TOPIC
	 * @param messages  多条消息
	 * @param timeout   超时时间
	 */
	public void sendBatch(String topic, List<Message> messages, long timeout) throws Exception{
		int size = 0, start = 0, end = 0;
		final int max = producer.getMaxMessageSize();
		for (Message msg : messages) {
			msg.setTopic(topic);
			size += getMessageSize(msg);
			if (size < max) {
				end ++;
			}else {
				producer.send(messages.subList(start, end), timeout);
				start = end;
				size = 0;
			}
		}
		if (start < end) {
			producer.send(messages.subList(start, end), timeout);
		}
	}
	
	/**
	 * 异步发送消息，Rocketmq不发回响应
	 * @param topic    TOPIC
	 * @param content  消息体
	 * @param headers  消息头(TAG,KEYS,WAIT,DELAY及其他)，发送完后会清空
	 * @throws Exception
	 */
	public void asyncSend(String topic, Object content, Map<String,Object> headers,
					SendCallback callback) throws Exception{
		Message message = this.toMessage(topic, content, headers);
		if (callback == null) {
			producer.sendOneway(message);
		}else {
			producer.send(message, callback);
		}
	}
	/**
	 * 异步发送消息，Rocketmq不发回响应
	 * @param topic    TOPIC
	 * @param content  消息体
	 * @param headers  消息头(TAG,KEYS,WAIT,DELAY及其他)，发送完后会清空
	 * @throws Exception
	 */
	public void asyncSend(String topic, Object content, Map<String,Object> headers,
					SendCallback callback, long timeout) throws Exception{
		Message message = this.toMessage(topic, content, headers);
		if (callback == null) {
			producer.sendOneway(message);
		}else {
			producer.send(message, callback, timeout);
		}
	}
	
	private Message toMessage(String topic, Object content, Map<String,Object> headers) throws IOException {
		Assert.hasText(topic, "'topic' must not be null");
		Assert.notNull(content, "message body must not be null");
		
		byte[] body = objectMapper.writeValueAsBytes(content);
		Message message = new Message(topic, body);
		if (headers != null) {
			message.setTags((String)headers.remove("TAG"));
			message.setKeys((String)headers.remove("KEYS"));
			Integer delayLevel = (Integer)headers.remove("DELAY");
			if (delayLevel != null) {
				message.setDelayTimeLevel(delayLevel);
			}
			if (headers.containsKey("WAIT")) {
				message.setWaitStoreMsgOK((Boolean)headers.remove("WAIT"));
			}
			for (Map.Entry<String,Object> entry : headers.entrySet()) {
				message.putUserProperty(entry.getKey(), 
								objectMapper.writeValueAsString(entry.getValue()));
			}
			headers.clear();
		}
		return message;
	}
	private final String doSend(Message message, long timeout) throws Exception{
		SendResult result = null;
		Exception t = null;
		String topic = message.getTopic();
		String tags = message.getTags();
		try {
			result = (timeout > 0) ? producer.send(message, timeout)
					: producer.send(message);
			return result.getMsgId();
		}catch(Exception e) {
			t = e;
			throw e;
		} finally {
			printResult(topic, tags, result, t);
			result = null;
			t = null;
		}
	}
	private static void printResult(String topic, String tags, SendResult result, Throwable e) {
		String target = topic;
		if (tags!=null && tags.length()>0) {
			target += (":"+tags);
		}
		if (e != null) {
			logger.error("send rocketmq(topic={}) error: ", target, e);
		} else {
			if (result!=null ) {//&& logger.isDebugEnabled()) {
				logger.info("sent a message to topic:{}. status:{}, msgId:{}",
							target, result.getSendStatus(), result.getMsgId());
			}
		}
	}
	/**
	 * 统计消息字节数
	 * @see MessageDecoder.encodeMessage(msg); 
	 * @see MessageClientIDSetter.setUniqID(msg);
	 */
	private final int getMessageSize(Message message) {
		byte[] props;
		try {
			props = MessageDecoder.messageProperties2String(message.getProperties()).getBytes("UTF-8");
		}catch(UnsupportedEncodingException e) {
			props = MessageDecoder.messageProperties2String(message.getProperties()).getBytes();
		}
		//UNIQ_KEY=C0A81FA435E818B4AAC21CE3B93D0000;   //总42位
		return message.getBody().length + 22 + props.length + 42;
	}
	
	public void start() throws MQClientException {
		producer.start();
	}
	public void destroy() {
		producer.shutdown();
	}
	
	public static class DefaultSendCallback implements SendCallback{
		private String tags;
		public DefaultSendCallback(String tags) {
			this.tags = tags;
		}

		@Override
		public void onSuccess(SendResult sendResult) {
			String topic = sendResult.getMessageQueue().getTopic();
			printResult(topic, tags, sendResult, null);
		}

		@Override
		public void onException(Throwable e) {
			printResult("UNKOWN-for-callback", tags, null, e);
		}
		
	}
	
//	public class MessageHeader{
//		private String tag;
//		private String keys;
//		private Map<String, String> properties = new HashMap<String, String>();
//		
//		public MessageHeader(String tag, String keys) {
//			this.tag = tag;
//			this.keys = keys;
//		}
//		
//		public void setProperty(String key, String value) {
//			this.properties.put(key, value);
//		}
//		public void setProperties(Map<String, String> properties) {
//			this.properties.putAll(properties);
//		}
//	}

}
