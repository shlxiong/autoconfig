package com.openxsl.config.rocketmq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import com.openxsl.config.rocketmq.core.ConsumeMode;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
//@qianniu.RocketMQMessageListener
public @interface ListenerMeta {
	
	String group();

    String topic();

//    SelectorType selectorType() default SelectorType.TAG;

    String selector() default "*";

    ConsumeMode consumeMode() default ConsumeMode.CONCURRENTLY;

    MessageModel messageModel() default MessageModel.CLUSTERING;

    int threadMax() default 64;
    
    int threadMin() default 4;

}
