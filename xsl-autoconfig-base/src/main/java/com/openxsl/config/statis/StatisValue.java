package com.openxsl.config.statis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * 统计数据视图
 * 
 * @author shuilin.xiong
 */
public class StatisValue {
	private String statisDate;      //统计日期(年、月、日）或区间
	private String name;            //统计维度
	private long orderNum;        	//订单数
	private long touristNum;      	//人次
	private BigDecimal totalPrice = new BigDecimal(new BigInteger("0"), 2);  //金额
	
	public StatisValue() {}
	
	public StatisValue(Map<String,?> map, String nameKey) {
		this.setName((String)map.get(nameKey));
		this.setOrderNum(map.get("orderNum")==null ? 0 : (Integer)map.get("orderNum"));
		this.setTouristNum(map.get("touristNum")==null ? 0 : (Integer)map.get("touristNum"));
		this.setTotalPrice(map.get("totalPrice")==null ? new BigDecimal(0.00): (BigDecimal)map.get("totalPrice"));
	}
	public void sum(StatisValue other) {
		if (!name.equals(other.getName())) {
			return;
		}
		orderNum += other.getOrderNum();
		touristNum += other.getTouristNum();
		if (other.getTotalPrice() != null) {
			totalPrice = totalPrice.add(other.getTotalPrice());
		}
	}
	
	public String getStatisDate() {
		return statisDate;
	}
	public void setStatisDate(String statisDate) {
		this.statisDate = statisDate;
	}
	public String getName() {
		return name;
	}
	public StatisValue setName(String name) {
		this.name = name;
		return this;
	}
	public long getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(long orderNum) {
		this.orderNum = orderNum;
	}
	public long getTouristNum() {
		return touristNum;
	}
	public void setTouristNum(long touristNum) {
		this.touristNum = touristNum;
	}
	public BigDecimal getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

}
