package com.openxsl.config.dal.echart;

public class NameNumber {
	private String name;
	private long num;
	
	public NameNumber() {}
	public NameNumber(String name, long num) {
		this.setName(name);
		this.setNum(num);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getNum() {
		return num;
	}
	public void setNum(long num) {
		this.num = num;
	}
	
}
