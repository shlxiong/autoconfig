package com.openxsl.config.kafka;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.openxsl.config.util.BeanUtils;

/**
 * 序列化和反序列化
 * 
 * @author xiongsl
 * @param <T>
 */
public class JacksonSerializer<T> implements Serializer<T>, Deserializer<T> {
//	private String encoding = "UTF8";
	private final ObjectMapper objectMapper;
	private Class<T> valueType;
	
	public JacksonSerializer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
//		String propertyName = isKey ? "key.serializer.encoding" : "value.serializer.encoding";
//        Object encodingValue = configs.get(propertyName);
//        if (encodingValue == null) {
//            encodingValue = configs.get("serializer.encoding");
//        }
//        if (encodingValue != null && encodingValue instanceof String) {
//            encoding = (String) encodingValue;
//        }
		valueType = (Class<T>)BeanUtils.getRawType(getClass(), 0);
	}

	@Override
	public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        } else {
            try {
				return objectMapper.writeValueAsBytes(data);
			} catch (JsonProcessingException e) {
				 throw new SerializationException(e);
			}
        }
	}
	
	@Override
	public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        } else {
            try {
				return objectMapper.readValue(data, valueType);
			} catch (IOException e) {
				throw new SerializationException(e);
			}
        }
    }

	@Override
	public void close() {
	}

}
