package com.openxsl.config.dal.echart;

import java.util.ArrayList;
import java.util.List;

public class EChartVO {
	private List<String> xList;        //X轴坐标
	private List<String> legend;       //图例
	private List<List<? extends Number>> yList = new ArrayList<List<? extends Number>>();   //Y轴坐标值，有n组数据
	
	EChartVO(){
	}

	public EChartVO(List<String> xList){
		 this.xList = xList;
	}

	public EChartVO(List<String> xList, List<String> legend){
		 this.xList = xList;
		 this.legend = legend;
	}

	public List<String> getXList() {
		return xList;
	}

	public void setXList(List<String> xList) {
		this.xList = xList;
	}

	public List<String> getLegend() {
		return legend;
	}

	public void setLegend(List<String> legend) {
		this.legend = legend;
	}

	public List<List<? extends Number>> getYList() {
		return yList;
	}

	public void setYList(List<List<? extends Number>> yList) {
		this.yList = yList;
	}
	
	public void addYList(List<? extends Number> data) {
		this.yList.add(data);
	}
	
}
