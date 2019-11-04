package com.openxsl.config.rocketmq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.rocketmq.core.RocketListener;
import com.openxsl.config.rocketmq.core.RocketListenerContainer;
import com.openxsl.config.util.SpringRegistry;

/**
 * 服务端的配置
 * @author xiongsl
 */
@ScanConfig
public class MessageListenerConfiguration implements ApplicationContextAware, InitializingBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private RocketProperties rocketProps;

    @Autowired(required = false)
    @Qualifier("rocketObjectMapper")
    private ObjectMapper objectMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	if (!rocketProps.isConsumers()) {
    		return;
    	}
    	
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(ListenerMeta.class);
        this.checkListenerPubSub(beans);
        if (beans != null) {
//        	beans.forEach(this::registerContainer);   //jdk8
        	for (Map.Entry<String,Object> entry : beans.entrySet()) {
        		this.registerContainer(entry.getKey(), entry.getValue());
        	}
        }
    }

    private void registerContainer(String beanName, Object bean) throws MQClientException {
        Class<?> clazz = AopUtils.getTargetClass(bean);
        if (!RocketListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + RocketListener.class.getName());
        }

        RocketListener<?> listener = (RocketListener<?>)bean;
        ListenerMeta annotation = clazz.getAnnotation(ListenerMeta.class);
        Map<String, Object> propertyMap = new HashMap<String,Object>();
        propertyMap.put("nameServer", rocketProps.getNamesrvAddr());
        propertyMap.put("instanceName", rocketProps.getInstanceName());
        propertyMap.put("vipChannel", rocketProps.isVipChannel());
        propertyMap.put("topic", annotation.topic());
        propertyMap.put("group", annotation.group());
        propertyMap.put("consumeMode", annotation.consumeMode());
        propertyMap.put("threadMax", annotation.threadMax());
        propertyMap.put("threadMin", annotation.threadMin());
        propertyMap.put("messageModel", annotation.messageModel());
        propertyMap.put("selector", annotation.selector());
        propertyMap.put("listener", listener);
//        if (objectMapper != null) {  //@Autowired
//            propertyMap.put("objectMapper", objectMapper);
//        }
        
        String containerBeanName = beanName + "Container";
        RocketListenerContainer container = SpringRegistry.register(containerBeanName, 
        				RocketListenerContainer.class, propertyMap, applicationContext);
        
        container.start();
        logger.info("register rocketMQ listener[topic={},selector={},group={}] to container[{}]",
        			annotation.topic(), container.getSelector(), annotation.group(), containerBeanName);
    }
    
    /**
     * 如果不幸某个topic，既登记为P2P又登记Pub/Sub，则P2P的listener收不到消息
     * @param beans Map<name,bean>
     */
    private void checkListenerPubSub(Map<String, Object> beans) {
    	Set<String> clusterTopics = new HashSet<String>();
    	Map<String, String> broadcastTopics = new HashMap<String, String>();
    	ListenerMeta annotation;
    	for (Map.Entry<String,Object> entry : beans.entrySet()) {
    		annotation = entry.getValue().getClass().getAnnotation(ListenerMeta.class);
    		if (annotation.messageModel() == MessageModel.CLUSTERING) {
    			clusterTopics.add(annotation.topic());
    		} else {
    			broadcastTopics.put(entry.getKey(), annotation.topic());
    		}
    	}
    	for (Map.Entry<String,String> entry : broadcastTopics.entrySet()) {
    		if (clusterTopics.contains(entry.getValue())) {
    			String errorMsg = String.format(
    					"请确认TOPIC [%s]-('%s') 不能同时指定为 'Pub/Sub'和'P2P'",
    					entry.getValue(), entry.getKey());
    			throw new IllegalStateException(errorMsg);
    		}
    	}
    }
}
