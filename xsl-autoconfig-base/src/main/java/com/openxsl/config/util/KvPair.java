package com.openxsl.config.util;

import java.io.Serializable;

/**
 * @author xiongsl
 */
@SuppressWarnings("serial")
public class KvPair implements Serializable{
	private String name;
	private Object value;
	
	public KvPair(String name, Object value){
		this.name = name;
		this.value = value;
	}
	@Override
	public String toString() {
		return name + ":" + value;
	}
	public String getName(){
		return name;
	}
	public Object getValue(){
		return value;
	}
}
