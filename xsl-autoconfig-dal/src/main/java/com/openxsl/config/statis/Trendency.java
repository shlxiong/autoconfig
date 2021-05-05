package com.openxsl.config.statis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

/**
 * 单个时间点的统计数量
 * 
 * @author shuilin.xiong
 */
public class Trendency extends StatisValue{
	private long lastOrderNum;        	//同期订单数
	private long lastTouristNum;      	//同期人次
	private BigDecimal lastTotalPrice = new BigDecimal(new BigInteger("0"), 2);  //同期金额 
	private String incrOrderNum = "--%";
	private String incrTouristNum = "--%";
	private String incrTotalPrice = "--%";
	
	public Trendency() {}
	
	public Trendency(StatisValue thisPeriod, StatisValue lastPeriod) {
		this.setName(thisPeriod.getName());
		this.setOrderNum(thisPeriod.getOrderNum());
		this.setTouristNum(thisPeriod.getTouristNum());
		this.setTotalPrice(thisPeriod.getTotalPrice());
		this.setLastValue(lastPeriod);
		this.incrOrderNum = super.increaseRate(this.getOrderNum(), lastOrderNum);
		this.incrTouristNum = super.increaseRate(this.getTouristNum(), lastTouristNum);
		this.incrTotalPrice = super.increaseRate(this.getTotalPrice(), lastTotalPrice);
	}
	
	public static List<Trendency> mergeLastPeriods(List<StatisValue> thisPeriod,
						List<StatisValue> lastPeriod) {
		List<Trendency> trendList = new ArrayList<Trendency>(thisPeriod.size()+2);
		Map<String, StatisValue> lastMap = lastPeriod.stream().collect(
					Collectors.toMap(StatisValue::getName, e->e));
	    for (StatisValue values : thisPeriod) {
	    	StatisValue lastOne = lastMap.remove(values.getName());
	    	trendList.add(new Trendency(values, lastOne));
	    }
	    //有可能X轴不一致
	    for (Map.Entry<String,StatisValue> entry : lastMap.entrySet()) {
	    	StatisValue thisOne = new StatisValue(entry.getKey());
	    	trendList.add(new Trendency(thisOne, entry.getValue()));
	    }
	    return trendList;
	}
	
	public static List<Trendency> mergeLastPeriods(List<StatisValue> thisPeriod,
							List<StatisValue> lastPeriod, Function<String,String> nameFunc) {
		Assert.notNull(nameFunc, "'nameFunc' can not be null");
		
		List<Trendency> trendList = new ArrayList<Trendency>(thisPeriod.size()+2);
		Map<String, StatisValue> lastMap = lastPeriod.stream().collect(
						Collectors.toMap(e->nameFunc.apply(e.getName()), e->e));
		for (StatisValue values : thisPeriod) {
			String name = nameFunc.apply(values.getName());
			StatisValue lastOne = lastMap.remove(name);
			trendList.add(new Trendency(values, lastOne));
		}
		//有可能X轴不一致
		for (Map.Entry<String,StatisValue> entry : lastMap.entrySet()) {
			String name = entry.getValue().getName(); //entry.getKey()
			StatisValue thisOne = new StatisValue(name);
			trendList.add(new Trendency(thisOne, entry.getValue()));
		}
		return trendList;
	}
	public NameNumber toNameNum(String attribute) {
		long lastNum = "touristNum".equals(attribute) ? lastTouristNum 
				: ("orderNum".equals(attribute)?lastOrderNum:lastTotalPrice.longValue());
		NameNumber nameNum = super.toNameNum(attribute);
		nameNum.setLastNum(lastNum);
		return nameNum;
	}
	
	@Override
	public void setLastValue(NameValue last) {
		if (last != null) {
			Assert.isTrue(last instanceof StatisValue, "类型不匹配");
			StatisValue lastPeriod = (StatisValue) last;
			this.setLastOrderNum(lastPeriod.getOrderNum());
			this.setLastTouristNum(lastPeriod.getTouristNum());
			this.setLastTotalPrice(lastPeriod.getTotalPrice());
		}
	}
	
	public StatisValue lastPeriod() {
		StatisValue statis = new StatisValue();
		statis.setName("last-"+this.getName());
		statis.setOrderNum(this.getLastOrderNum());
		statis.setTouristNum(this.getLastTouristNum());
		statis.setTotalPrice(this.getLastTotalPrice());
		return statis;
	}
	public StatisValue thisPeriod() {
		StatisValue statis = new StatisValue();
		statis.setName(this.getName());
		statis.setOrderNum(this.getOrderNum());
		statis.setTouristNum(this.getTouristNum());
		statis.setTotalPrice(this.getTotalPrice());
		return statis;
	}
	
	public NameNumber toNameNumLast(String attribute) {
		long num = "touristNum".equals(attribute) ? lastTouristNum 
				: ("orderNum".equals(attribute)?lastOrderNum:lastTotalPrice.longValue());
		return new NameNumber(this.getName(), num);
	}
	
//	public BigDecimal getSalesAmount() {
//		return this.getTotalPrice();
//	}
	public long getLastOrderNum() {
		return lastOrderNum;
	}
	public void setLastOrderNum(long lastOrderNum) {
		this.lastOrderNum = lastOrderNum;
	}
	public long getLastTouristNum() {
		return lastTouristNum;
	}
	public void setLastTouristNum(long lastTouristNum) {
		this.lastTouristNum = lastTouristNum;
	}
	public BigDecimal getLastTotalPrice() {
		return lastTotalPrice;
	}
	public void setLastTotalPrice(BigDecimal lastTotalPrice) {
		this.lastTotalPrice = lastTotalPrice;
	}

	public String getIncrOrderNum() {
		return incrOrderNum;
	}
	public void setIncrOrderNum(String incrOrderNum) {
		this.incrOrderNum = incrOrderNum;
	}
	public String getIncrTouristNum() {
		return incrTouristNum;
	}
	public void setIncrTouristNum(String incrTouristNum) {
		this.incrTouristNum = incrTouristNum;
	}
	public String getIncrTotalPrice() {
		return incrTotalPrice;
	}
	public void setIncrTotalPrice(String incrTotalPrice) {
		this.incrTotalPrice = incrTotalPrice;
	}

}
