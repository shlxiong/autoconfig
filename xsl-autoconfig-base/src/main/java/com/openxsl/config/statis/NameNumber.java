package com.openxsl.config.statis;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NameNumber implements NameValue {
	private String name;
	private long num;
	private long lastNum;
	private String incrRate = "--%";   //增长率
	private String ratio = "--%";
	
	private transient long total;
	
	public static void mergeLastPeriods(List<NameNumber> thisPeriod, List<NameNumber> lastPeriod) {
		Map<String, NameNumber> lastMap = lastPeriod.stream().collect(
					Collectors.toMap(NameNumber::getName, e->e));
        for (NameNumber nameNum : thisPeriod) {
        	nameNum.setLastNum(lastMap.remove(nameNum.getName()));
        }
        //有可能X轴不一致
        for (Map.Entry<String,NameNumber> entry : lastMap.entrySet()) {
        	NameNumber thisOne = new NameNumber(entry.getKey(), 0L);
        	thisOne.setLastNum(entry.getValue());
        	thisPeriod.add(thisOne);
        }
	}
	
	public NameNumber() {}
	public NameNumber(String name, long num) {
		this.setName(name);
		this.setNum(num);
	}
	
	public boolean equals(Object other) {
		if (other==null || !(other instanceof NameNumber)) {
			return false;
		}
		return name.equals(((NameNumber)other).name);
	}
	public void setLastNum(NameNumber that) {
		if (that != null) {
			this.setLastNum(that.num);
		}
	}
	public void setLastNum(long lastNum) {
		if (lastNum != 0) {
			BigDecimal deta = new BigDecimal(100 * (num-lastNum));
			BigDecimal last = new BigDecimal(Math.abs(lastNum));
			incrRate = deta.divide(last, 2,  BigDecimal.ROUND_HALF_UP) + "%";
		}
		this.lastNum = lastNum;
	}
	public void setTotal(long total) {
		if (total > 0) {
			BigDecimal dd = new BigDecimal(100 * num);
			ratio = dd.divide(new BigDecimal(total), 2, BigDecimal.ROUND_HALF_UP) + "%";
			this.total = total;
		}
	}
	public long getTotal() {
		return this.total;
	}
	
	public String getName() {
		return name;
	}
	public NameNumber setName(String name) {
		this.name = name;
		return this;
	}
	public Long getNum() {
		return num;
	}
	public void setNum(long num) {
		this.num = num;
	}
	public long getLastNum() {
		return this.lastNum;
	}
	
	public String getIncrRate() {
		return incrRate;
	}
	public String getRatio() {
		return ratio;
	}
//	public void setIncrRate(BigDecimal incrRate) {
//		this.incrRate = incrRate;
//	}
	
	public static void main(String[] args) {
		NameNumber nn = new NameNumber("", 56);
		nn.setLastNum(new NameNumber("", 0));
		System.out.println(nn.getIncrRate().toString());
	}

}
