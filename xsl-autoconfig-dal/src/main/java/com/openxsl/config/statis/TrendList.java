package com.openxsl.config.statis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 多点本期同期统计趋势
 * 
 * @author shuilin.xiong
 */
@SuppressWarnings("serial")
public class TrendList extends ArrayList<StatisValue>{
public static final TrendList EMPTY = new TrendList(Collections.emptyList(), false);
	
	public TrendList(List<? extends StatisValue> data, boolean appendTotal) {
		long total1 = 0, total2 = 0 ;
		BigDecimal totalPrice = new BigDecimal(new BigInteger("0"), 2);
		StatisValue totalItem = new StatisValue("Total");
		for (StatisValue values : data) {
			total1 += values.getOrderNum();
			total2 += values.getTouristNum();
			totalPrice = totalPrice.add(values.getTotalPrice());
		}
		totalItem.setOrderNum(total1);
		totalItem.setTouristNum(total2);
		totalItem.setTotalPrice(totalPrice);
		for (StatisValue values : data) {
			values.setTotal(totalItem);
		}
		this.addAll(data);
		if (appendTotal) {
			this.add(totalItem);
		}
	}
	
}
