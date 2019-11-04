package com.openxsl.config.kafka.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.util.Assert;

import com.openxsl.config.autodetect.PrefixPropsRegistrar;
import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.kafka.ConsumerProperties;
import com.openxsl.config.loader.PrefixProperties;
import com.openxsl.config.util.MapUtils;

@ScanConfig
@EnableKafka
@Import(PrefixPropsRegistrar.class)
public class ConsumerConfiguration extends ProducerConfiguration {
	
	@Bean
    public ConsumerProperties consumerProperties() throws Exception {
		Properties consumerProps = PrefixProperties.prefixProperties(kafkaProps, "consumer", true);
		String serversKey = ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
		MapUtils.putIfAbsent(consumerProps, serversKey, consumerProps.getProperty("bootstrap-servers"));
		MapUtils.putIfAbsent(consumerProps, serversKey, kafkaProps.getProperty(serversKey));
		MapUtils.putIfAbsent(consumerProps, serversKey, kafkaProps.getProperty("bootstrap-servers"));
		Assert.notNull(consumerProps.getProperty(serversKey), "kafka consumer servers is null");
		String groupKey = ConsumerConfig.GROUP_ID_CONFIG;
		MapUtils.putIfAbsent(consumerProps, groupKey, consumerProps.getProperty("group-id"));
		Assert.notNull(consumerProps.getProperty(groupKey), "kafka consumer group is null");
		MapUtils.putIfAbsent(consumerProps, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, DEFAULT_DESERIALIZER);
		MapUtils.putIfAbsent(consumerProps, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DEFAULT_DESERIALIZER);
		
		Map<String,Object> configs = new HashMap<String,Object>();
		for (Map.Entry<?,?> entry : consumerProps.entrySet()) {
			String propKey = entry.getKey().toString();
			if (propKey.indexOf("-") != -1) {
				propKey = propKey.replace('-', '.');
			}
			configs.put(propKey, entry.getValue());
		}
		consumerProps.clear();
		return new ConsumerProperties(configs);
    }
	
	@Bean
    public ConsumerFactory<String,Object> consumerFactory(ConsumerProperties consumerProperties) {
        //return new DefaultKafkaConsumerFactory<String,Object>(consumerProperties.getConfigs());
		return new DispatchConsumerFactory<String,Object>(consumerProperties.getConfigs());
    }

	@Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,Object>>
					kafkaListenerContainerFactory(ConsumerFactory<String,Object> consumerFactory) throws Exception {
		Properties listenerProps = PrefixProperties.prefixProperties(kafkaProps, "listener", true);
        ConcurrentKafkaListenerContainerFactory<String,Object> factory =
                	new ConcurrentKafkaListenerContainerFactory<String,Object>();
        factory.setConsumerFactory(consumerFactory);
        int concurrency = Integer.parseInt(listenerProps.getProperty("concurrency", "1"));
        factory.setConcurrency(concurrency);
        int pollTimeout = Integer.parseInt(listenerProps.getProperty("poll.timeout", "3000"));
        factory.getContainerProperties().setPollTimeout(pollTimeout);
        return factory;
    }
	
}
