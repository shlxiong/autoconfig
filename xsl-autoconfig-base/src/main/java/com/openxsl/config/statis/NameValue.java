package com.openxsl.config.statis;

public interface NameValue {
	
	public String getName();
	
	default <T> T getValue() { return null; }
	
	default Number getNum() { return null; }
	
}

