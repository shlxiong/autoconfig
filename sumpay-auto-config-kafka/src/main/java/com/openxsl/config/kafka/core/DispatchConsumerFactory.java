package com.openxsl.config.kafka.core;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.openxsl.config.util.StringUtils;

public class DispatchConsumerFactory<K,V> extends DefaultKafkaConsumerFactory<K, V> {

	public DispatchConsumerFactory(Map<String, Object> configs) {
		super(configs);
	}

	//public Consumer<K, V> createConsumer(String groupId, String clientIdSuffix)
	protected KafkaConsumer<K, V> createKafkaConsumer(String groupId, String clientIdSuffix) {
		Map<String, Object> configs = this.getConfigurationProperties();   //TODO("XIONGSL")
		boolean shouldModifyClientId = configs.containsKey(ConsumerConfig.CLIENT_ID_CONFIG)
				&& clientIdSuffix != null;
		if (groupId == null && !shouldModifyClientId) {
			return createKafkaConsumer();
		}
		else {
			Map<String, Object> modifiedConfigs = new HashMap<>(configs);
			if (groupId != null) {
				modifiedConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			}
			if (shouldModifyClientId) {
				modifiedConfigs.put(ConsumerConfig.CLIENT_ID_CONFIG,
					modifiedConfigs.get(ConsumerConfig.CLIENT_ID_CONFIG) + clientIdSuffix);
			}
			//TODO("XIONGSL")
			this.resetValueSerializer(groupId, modifiedConfigs);
			return new KafkaConsumer<K, V>(modifiedConfigs, null, null);
		}
	}
	
	//TODO groupId不对, 应该取注解的值
	private void resetValueSerializer(String groupId, Map<String, Object> configs) {
		String customedStr = (String)configs.get("properties.deserializers");
		if (!StringUtils.isEmpty(customedStr)) {
			String deserializer = null;
			StringTokenizer tokens = new StringTokenizer(customedStr, "=, {}");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				if (token.equals(groupId)) {
					deserializer = tokens.nextToken();
					break; 
				}
			}
			if (deserializer != null) {
				configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
			}
		}
	}
	
}
