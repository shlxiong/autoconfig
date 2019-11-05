package com.openxsl.tracing.registry.model;

import java.io.Serializable;
import java.util.Map;

/**
 * 服务的唯一标识
 * @author xiongsl
 */
public interface ServiceKey extends Serializable {
	
	public String serialize();
	
	public Map<String,Serializable> deserialize();

}
