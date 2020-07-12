package com.openxsl.config.dal.echart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 多维坐标的图标（如热力图）
 * 
 * @author shuilin.xiong
 */
@SuppressWarnings("rawtypes")
public class EChartVO2 {
	private List<String> xList;        //X轴坐标
	private List<String> legend;       //图例
	private List<List<Map>> yList = new ArrayList<List<Map>>();   //Y轴坐标值，有n组数据
	
	EChartVO2(){
	}

	public EChartVO2(List<String> xList){
		 this.xList = xList;
	}

	public EChartVO2(List<String> xList, List<String> legend){
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

	public List<List<Map>> getYList() {
		return yList;
	}

	public void setYList(List<List<Map>> yList) {
		this.yList = yList;
	}
	
	public void addYList(List<Map> data) {
		this.yList.add(data);
	}
	
}
