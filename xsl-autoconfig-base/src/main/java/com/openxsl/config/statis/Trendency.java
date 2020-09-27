package com.openxsl.config.statis;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 单个时间点的统计数量
 * 
 * @author shuilin.xiong
 */
public class Trendency extends StatisValue{
		private long lastOrderNum;        	//同期订单数
		private long lastTouristNum;      	//同期人次
		private BigDecimal lastTotalPrice = new BigDecimal(new BigInteger("0"), 2);  //同期金额 
		
		public Trendency() {}
		
		public Trendency(StatisValue thisPeriod, StatisValue lastPeriod) {
			this.setOrderNum(thisPeriod.getOrderNum());
			this.setTouristNum(thisPeriod.getTouristNum());
			this.setTotalPrice(thisPeriod.getTotalPrice());
			this.setLastOrderNum(lastPeriod.getOrderNum());
			this.setLastTouristNum(lastPeriod.getTouristNum());
			this.setLastTotalPrice(lastPeriod.getTotalPrice());
		}
		
		public StatisValue getLastPeriod() {
			StatisValue statis = new StatisValue();
			statis.setName("last-"+this.getName());
			statis.setOrderNum(this.getLastOrderNum());
			statis.setTouristNum(this.getLastTouristNum());
			statis.setTotalPrice(this.getLastTotalPrice());
			return statis;
		}
		
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

}
