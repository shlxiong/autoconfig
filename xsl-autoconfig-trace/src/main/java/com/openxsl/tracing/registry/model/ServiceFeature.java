package com.openxsl.tracing.registry.model;

import java.io.Serializable;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public interface ServiceFeature extends Serializable {
	
	public String serialize();
	
	public Map<String,Serializable> deserialize();
	
	@SuppressWarnings("serial")
	public abstract class BaseServiceFeature implements ServiceFeature{
		
		@Override
		public String serialize() {
			return JSON.toJSONString(this.deserialize());
		}
	}

}
