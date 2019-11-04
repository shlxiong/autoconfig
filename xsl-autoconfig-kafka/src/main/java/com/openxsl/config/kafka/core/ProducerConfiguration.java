package com.openxsl.config.kafka.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.openxsl.config.autodetect.PrefixProps;
import com.openxsl.config.autodetect.PrefixPropsRegistrar;
import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.kafka.ProducerProperties;
import com.openxsl.config.loader.PrefixProperties;
import com.openxsl.config.util.MapUtils;

/**
 * 
 * @author xiongsl
 */
@ScanConfig
@Import(PrefixPropsRegistrar.class)
public class ProducerConfiguration {
	protected final String DEFAULT_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
	protected final String DEFAULT_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
	
	@Resource
	@PrefixProps(prefix="spring.kafka", rewrite=true)
	protected Properties kafkaProps;    
	
	public void setKafkaProps(Properties kafkaProps) {
		this.kafkaProps = kafkaProps;
	}
	
	@Bean
	public ProducerProperties producerProperties() throws Exception {
		Properties producerProps = PrefixProperties.prefixProperties(kafkaProps, "producer", true);
		String serversKey = ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
		MapUtils.putIfAbsent(producerProps, serversKey, producerProps.getProperty("bootstrap-servers"));
		MapUtils.putIfAbsent(producerProps, serversKey, kafkaProps.getProperty(serversKey));
		MapUtils.putIfAbsent(producerProps, serversKey, kafkaProps.getProperty("bootstrap-servers"));
		Assert.notNull(producerProps.getProperty(serversKey), "kafka producer servers is null");
		MapUtils.putIfAbsent(producerProps, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, DEFAULT_SERIALIZER);
		MapUtils.putIfAbsent(producerProps, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DEFAULT_SERIALIZER);
		
		Map<String,Object> configs = new HashMap<String,Object>();
		for (Map.Entry<?,?> entry : producerProps.entrySet()) {
			String propKey = entry.getKey().toString();
			if (propKey.indexOf("-") != -1) {
				propKey = propKey.replace('-', '.');
			}
			configs.put(propKey, entry.getValue());
		}
		producerProps.clear();
		return new ProducerProperties(configs);
	}
	
	@Bean
	public ProducerFactory<String,Object> producerFactory( //@Autowired
							ProducerProperties producerProperties) {
		DefaultKafkaProducerFactory<String,Object> producerFactory
					= new DefaultKafkaProducerFactory<String,Object>(producerProperties.getConfigs());
		String prefixTransId = (String)producerProperties.getValue("transaction.id.prefix");
		if (prefixTransId != null) {
			producerFactory.setTransactionIdPrefix(prefixTransId);
		}
		return producerFactory;
	}
	
	@Bean
	public KafkaTemplate<String,Object> kafkaTemplate(ProducerFactory<String,Object> producerFactory) {
		String autoFlush = kafkaProps.getProperty("template.auto-flush",
						kafkaProps.getProperty("template.auto.flush","true"));
		KafkaTemplate<String,Object> template = new KafkaTemplate<String,Object>(
						producerFactory, Boolean.valueOf(autoFlush));
		String defaultTopic = kafkaProps.getProperty("template.default-topic",
						kafkaProps.getProperty("template.default.topic"));
		template.setDefaultTopic(defaultTopic);
		return template;
	}
	
	@Bean("kafkaObjectMapper")
    public ObjectMapper messageObjectMapper() {
        return new ObjectMapper();
    }

}
