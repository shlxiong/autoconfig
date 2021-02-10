package com.openxsl.config.statis;

public class NameNumber2 implements NameValue {
	private String name;
	private long num;
	private long prevNum;    //环比值
	private long lastNum;    //同比值
	
	private String prevIncrRate = "--%";
	private String lastIncrRate = "--%";
	
	public NameNumber2() {}
	
	public NameNumber2(String name, long num, long prevNum, long lastNum) {
		this.setName(name);
		this.setNum(num);
		this.setPrevNum(prevNum);
		this.setLastNum(lastNum);
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public Long getNum() {
		return num;
	}

	public void setNum(long num) {
		this.num = num;
	}

	public long getPrevNum() {
		return prevNum;
	}

	public void setPrevNum(long prevNum) {
		this.prevNum = prevNum;
		this.prevIncrRate = this.increaseRate(num, prevNum);
	}

	public long getLastNum() {
		return lastNum;
	}

	public void setLastNum(long lastNum) {
		this.lastNum = lastNum;
		this.lastIncrRate = this.increaseRate(num, lastNum);
	}

	public String getPrevIncrRate() {
		return prevIncrRate;
	}

	public String getLastIncrRate() {
		return lastIncrRate;
	}

	public void setName(String name) {
		this.name = name;
	}

}
