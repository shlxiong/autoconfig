package com.openxsl.config.rocketmq;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionCheckListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.rocketmq.core.RocketTemplate;
import com.openxsl.config.thread.GrouppedThreadFactory;
import com.openxsl.config.util.BeanUtils;

/**
 * 消息发送者的配置
 * @author xiongsl
 */
@ScanConfig
public class ProducerConfiguration {
	
	@SuppressWarnings("deprecation")
	@Bean("rocketProducer")
    public DefaultMQProducer mqProducer(RocketProperties rocketProps) {
		if (!rocketProps.isProcuders()){
			return null;
		}
		String groupName = rocketProps.getProducerGroup();
		DefaultMQProducer producer = null;
		String transactionListener = rocketProps.getTransactionListener();
		if (transactionListener.length() > 0) {
			try {
				TransactionCheckListener listener = BeanUtils.instantiate(
							transactionListener, TransactionCheckListener.class);
				TransactionMQProducer txProducer = new TransactionMQProducer(groupName);
				txProducer.setTransactionCheckListener(listener);
				txProducer.setCheckThreadPoolMaxSize(2);
				txProducer.setCheckThreadPoolMinSize(2);
//				txProducer.setCheckRequestHoldMax(2000);   //默认就是2000
				producer = txProducer;
			}catch(Exception e) {
			}
		}
		if (producer == null) {
			producer = new DefaultMQProducer(groupName);
		}
        producer.setNamesrvAddr(rocketProps.getNamesrvAddr());
        producer.setInstanceName(rocketProps.getInstanceName());
        producer.setVipChannelEnabled(rocketProps.isVipChannel());
        producer.setSendMsgTimeout(rocketProps.getSendMsgTimeout());
        producer.setMaxMessageSize(rocketProps.getMaxMessageSize());
        producer.setRetryTimesWhenSendFailed(rocketProps.getRetryTimesWhenFailed());
        producer.setRetryTimesWhenSendAsyncFailed(rocketProps.getRetryTimesWhenAsyncFailed());
        producer.setCompressMsgBodyOverHowmuch(rocketProps.getCompressMsgBodyOverHowmuch());
//        producer.setRetryAnotherBrokerWhenNotStoreOK(rocketProps.isRetryAnotherBrokerWhenNotStoreOk());
        producer.setAsyncSenderExecutor(new GrouppedThreadFactory("AsyncMQSender")
        				.newThreadPool(1, 10, 60));
        
        Iterator<SendMessageHook> hooksIterator = ServiceLoader.load(SendMessageHook.class).iterator();
        while (hooksIterator.hasNext()) {
            producer.getDefaultMQProducerImpl().registerSendMessageHook(hooksIterator.next());
        }
        return producer;
    }

    @Bean("rocketObjectMapper")
    public ObjectMapper messageObjectMapper() {
        return new ObjectMapper();
    }

    @Bean(name="rocketTemplate", initMethod="start", destroyMethod="destroy")
    @DependsOn({"rocketProducer", "rocketObjectMapper"})
    public RocketTemplate rocketMQTemplate(DefaultMQProducer mqProducer,
            					ObjectMapper objectMapper) {
    	return (mqProducer == null) ? null : new RocketTemplate();
    }

}
