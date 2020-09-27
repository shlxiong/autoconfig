package com.openxsl.config.statis;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

/**
 * 多点本期同期统计趋势
 * 
 * @author shuilin.xiong
 */
public class TrendList {
	private List<StatisValue> thisPeriods;
	private List<StatisValue> lastPeriods;
	
	public static final TrendList EMPTY = new TrendList(
					new ArrayList<StatisValue>(0), new ArrayList<StatisValue>(0));
	
	public TrendList(List<StatisValue> thisPeriods, List<StatisValue> lastPeriods) {
		this.setThisPeriods(thisPeriods);
		this.setLastPeriods(lastPeriods);
	}
	
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public boolean isEmpty() {
		return thisPeriods.isEmpty() && lastPeriods.isEmpty();
	}

	public List<StatisValue> getThisPeriods() {
		return thisPeriods;
	}

	public void setThisPeriods(List<StatisValue> thisPeriods) {
		this.thisPeriods = thisPeriods;
	}

	public List<StatisValue> getLastPeriods() {
		return lastPeriods;
	}

	public void setLastPeriods(List<StatisValue> lastPeriods) {
		this.lastPeriods = lastPeriods;
	}

}
