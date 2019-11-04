package com.openxsl.config.rocketmq.core;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 服务端消息容器
 * @author xiongsl
 */
public class RocketListenerContainer implements InitializingBean, DisposableBean{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ObjectMapper objectMapper;
	private DefaultMQPushConsumer consumer;
	
	private String nameServer;
	private String instanceName;
	private boolean vipChannel;
	private String group;
	private String topic;
	private int threadMax = 64;
	private int threadMin = 4;
	private String selector;
	private MessageModel messageModel;
	private ConsumeMode consumeMode;
	private RocketListener<Object> listener;
	private Class<?> messageType = Object.class;
	
	private long suspendCurrentQueueTimeMillis = 1000;
	private int delayLevelWhenNextConsume = 0;
	
	@Override
	public void afterPropertiesSet() {
		Assert.hasText(nameServer, "'socketmq's nameServerAddr' must not be null");
		Assert.hasText(topic, "'topic' must not be null");
		Assert.hasText(group, "'group' must not be null");
		Assert.notNull(listener, "message listener must not be null");
		
		this.decideMessageType();
		
		consumer = new DefaultMQPushConsumer(group);
		consumer.setNamesrvAddr(nameServer);
		consumer.setInstanceName(instanceName);
		consumer.setVipChannelEnabled(vipChannel);
		consumer.setConsumeThreadMin(threadMin);
		consumer.setConsumeThreadMax(threadMax);
		consumer.setMessageModel(messageModel);  //CLUSTER, BROADCASTING
	}
	
	public void start() throws MQClientException {
		this.registerConsumeMessageHook();
		this.subscribe();
		
		if (consumeMode == ConsumeMode.CONCURRENTLY) {
			consumer.registerMessageListener(new MessageListenerConcurrently() {
				@Override
				public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, 
								ConsumeConcurrentlyContext context) {
					for (MessageExt messageExt : msgs) {
		                try {
		                	RocketListenerContainer.this.consumeMessage(messageExt);
		                } catch (Exception e) {
		                    logger.warn("concurrently consume message failed: {}", messageExt, e);
		                    context.setDelayLevelWhenNextConsume(delayLevelWhenNextConsume);
		                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
		                }
		            }
					return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
				}
			});
		}else {
			consumer.registerMessageListener(new MessageListenerOrderly() {
				@Override
				public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, 
								ConsumeOrderlyContext context) {
					for (MessageExt messageExt : msgs) {
		                try {
		                	RocketListenerContainer.this.consumeMessage(messageExt);
		                } catch (Exception e) {
		                    logger.warn("orderly consume message failed: {}", messageExt, e);
		                    context.setSuspendCurrentQueueTimeMillis(suspendCurrentQueueTimeMillis);
		                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
		                }
		            }
		            return ConsumeOrderlyStatus.SUCCESS;
				}
			});
		}
		
		consumer.start();
	}
	
	@Override
	public void destroy() {
		consumer.shutdown();
	}
	
	private void decideMessageType() {
		Type[] interfaces = listener.getClass().getGenericInterfaces();
        for (Type type : interfaces) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() == RocketListener.class) {
            	messageType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
            	break;
            }
        }
	}
	private void registerConsumeMessageHook() {
		Iterator<ConsumeMessageHook> hooksIterator = ServiceLoader.load(
						ConsumeMessageHook.class).iterator();
        while (hooksIterator.hasNext()) {
        	consumer.getDefaultMQPushConsumerImpl().registerConsumeMessageHook(
        				hooksIterator.next() );
        }
	}
	private void subscribe() throws MQClientException {
		MessageSelector messageSelector = null;
		if (StringUtils.hasText(selector)) {
			if ("*".equals(selector)
					|| Pattern.matches("\\w+((\\s)*\\|\\|(\\s)*\\w+)*", selector)) {
				//单个tag或类似“tag1 || tag2”
				messageSelector = MessageSelector.byTag(selector);
				selector = String.format("{TAG:%s}", selector);
				consumer.subscribe(topic, messageSelector);
			} else if (selector.startsWith("filter=")){
				//自定义代码 org.apache.rocketmq.common.filter.MessageFilter
				String filter = selector.substring("filter=".length()).trim();
				URL url = ClassUtils.getDefaultClassLoader().getResource(filter.replace('.', '/')+".java");
				String filterCode = MixAll.file2String(url);
				selector = String.format("{source:%s}", filter);
				consumer.subscribe(topic, filter, filterCode);
			} else {
				messageSelector = MessageSelector.bySql(selector);
				selector = String.format("{SQL:%s}", selector);
				consumer.subscribe(topic, messageSelector);
			}
		}
	}
	private final void consumeMessage(MessageExt message) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("The MessageContainer received a msg: {}", message);
		}
		if (MessageExt.class.isAssignableFrom(messageType)) {
			listener.onMessage(message);
		}else {
			Object content = objectMapper.readValue(message.getBody(), messageType);
			listener.onMessage(content);
		}
	}
	//===================================================
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	public DefaultMQPushConsumer getConsumer() {
		return consumer;
	}
	public void setConsumer(DefaultMQPushConsumer consumer) {
		this.consumer = consumer;
	}
	public String getNameServer() {
		return nameServer;
	}
	public void setNameServer(String nameServer) {
		this.nameServer = nameServer;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
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
	public String getSelector() {
		return selector;
	}
	public void setSelector(String selector) {
		this.selector = selector;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public MessageModel getMessageModel() {
		return messageModel;
	}
	public void setMessageType(MessageModel messageModel) {
		this.messageModel = messageModel;
	}
	public ConsumeMode getConsumeMode() {
		return consumeMode;
	}
	public void setConsumeMode(ConsumeMode consumeMode) {
		this.consumeMode = consumeMode;
	}
	public RocketListener<?> getListener() {
		return listener;
	}
	public void setListener(RocketListener<Object> listener) {
		this.listener = listener;
	}
	public void setMessageModel(MessageModel messageModel) {
		this.messageModel = messageModel;
	}

	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public boolean getVipChannel() {
		return vipChannel;
	}
	public void setVipChannel(boolean vipChannel) {
		this.vipChannel = vipChannel;
	}

}
