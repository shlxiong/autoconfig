package com.openxsl.config.statis;

import java.math.BigDecimal;
import java.util.List;

/**
 * TOP-N的统计值，带上总量
 * 
 * @author shuilin.xiong
 */
public class TopStatisValue {
	private BigDecimal total = new BigDecimal(0);
	private List<StatisValue> values;
	
	public TopStatisValue(List<StatisValue> values, int tops, String totalName) {
		tops = tops < 1 ? values.size() : tops;
		this.values = tops>=values.size() ? values : values.subList(0, tops);
		for (StatisValue value : values) {
			if ("touristNum".equals(totalName)) {
				total = total.add(new BigDecimal(value.getTouristNum()));
			} else if ("orderNum".equals(totalName)){
				total = total.add(new BigDecimal(value.getOrderNum()));
			}else {
				total = total.add(value.getTotalPrice());
			}
		}
	}
	
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public List<StatisValue> getValues() {
		return values;
	}
	public void setValues(List<StatisValue> values) {
		this.values = values;
	}
	public int getCount() {
		return values==null ? 0 : values.size();
	}
	
}
