package com.openxsl.config.statis;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

public class NameNumber implements NameValue {
	private String name;
	private long num;
	private long lastNum;
	private String incrRate = "--%";   //增长率
	private String ratio = "--%";
	
	private transient long total;
	
	public NameNumber() {}
	public NameNumber(String name, long num) {
		this.setName(name);
		this.setNum(num);
	}
	
	public static void mergeLastPeriods(List<NameNumber> thisPeriod, List<NameNumber> lastPeriod) {
		Map<String, NameNumber> lastMap = lastPeriod.stream().collect(
					Collectors.toMap(NameNumber::getName, e->e));
        for (NameNumber nameNum : thisPeriod) {
        	nameNum.setLastValue(lastMap.remove(nameNum.getName()));
        }
        //有可能X轴不一致
        for (Map.Entry<String,NameNumber> entry : lastMap.entrySet()) {
        	NameNumber thisOne = new NameNumber(entry.getKey(), 0L);
        	thisOne.setLastValue(entry.getValue());
        	thisPeriod.add(thisOne);
        }
	}
	
	@Override
	public String getName() {
		return name;
	}
	@Override
	public Long getNum() {
		return num;
	}
	
	@Override
	public void setLastValue(NameValue last) {
		if (last != null) {
			Assert.isTrue(last instanceof NameNumber, "类型不匹配");
			this.setLastNum(((NameNumber)last).num);
		}
	}
	public void setLastNum(long lastNum) {
		if (lastNum != 0) {
			this.lastNum = lastNum;
			incrRate = this.increaseRate(num, lastNum);
		}
	}
	public void setTotal(long total) {
		if (total > 0) {
			this.total = total;
			ratio = NameValue.percent(new BigDecimal(num), new BigDecimal(total));
		}
	}
	public long getTotal() {
		return this.total;
	}
	
	public boolean equals(Object other) {
		if (other==null || !(other instanceof NameNumber)) {
			return false;
		}
		return name.equals(((NameNumber)other).name);
	}
	
	public NameNumber setName(String name) {
		this.name = name;
		return this;
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
		nn.setLastValue(new NameNumber("", 0));
		System.out.println(nn.getIncrRate().toString());
	}

}
