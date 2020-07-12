package com.openxsl.config.dal.echart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.openxsl.config.util.BeanUtils;
import com.openxsl.config.util.StringUtils;

/**
 * echarts转换类
 * @author shuilin.xiong
 */
public final class EchartUtils {
	
	/**
	 * 转换为echart的数据格式（legend，xlist:name，ylist:1~3个指标）
	 * @param statisValues 统计值
	 * @param attributes 逗号分隔的属性名
	 * @param legends 图例名称
	 */
	public static final EChartVO toChartVO(
					List<StatisValue> statisValues,	String attributes,
					String legends){
		int len = statisValues.size();
		List<String> xlabels = new ArrayList<String>(len);
		for (StatisValue statis : statisValues) {
			xlabels.add(statis.getName());
		}
		EChartVO echart = new EChartVO(xlabels);
		if (!StringUtils.isEmpty(legends)) {
			echart.setLegend(Arrays.asList(legends.split(",")));
		}
		
		String[] fields = (StringUtils.isEmpty(attributes) ? "totalPrice,touristNum": attributes)
						.split(",");
		for (String field : fields) {
			List<Number> data = new ArrayList<Number>();
			for (StatisValue statis : statisValues) {
				data.add((Number)BeanUtils.getPrivateField(statis, field));
			}
			echart.addYList(data);
		}
		
		return echart;
	}
	
	/**
	 * 转换为本期同期（月日）的双线图
	 * @param thisPeriods 本期数据
	 * @param lastPeriods 同期数据
	 * @param attribute 单个属性名
	 */
	public static final EChartVO toChartVO(String attribute, 
					List<StatisValue> thisPeriods, List<StatisValue> lastPeriods){
		Function<String,String> func = new Function<String,String>() {
			@Override
			public String apply(String t) {
				return t==null||t.length()<5 ? t : t.substring(5);
			}
			
		};
		return toChartVO(null, attribute, thisPeriods, lastPeriods, func);
	}
	
	/**
	 * 两个共同属性的序列组成一张图表，以序列1的X轴为准
	 * @param legends   图例名称
	 * @param attribute 属性名
	 * @param values1      序列1
	 * @param values2      序列2
	 * @param labelFunc 可选
	 */
	public static final EChartVO toChartVO(String legends, String attribute,
					List<StatisValue> values1, List<StatisValue> values2,
					Function<String,String> labelFunc){
		int len = values1.size();
		List<String> xlabels = new ArrayList<String>(len);
		List<Number> data = new ArrayList<Number>();
		for (StatisValue statis : values1) {
			xlabels.add(statis.getName());
			data.add((Number)BeanUtils.getPrivateField(statis, attribute));
		}
		EChartVO echart = new EChartVO(xlabels);
		if (!StringUtils.isEmpty(legends)) {
			echart.setLegend(Arrays.asList(legends.split(",")));
		} else {
			echart.setLegend(Lists.newArrayList("本期", "同期"));
		}
		echart.addYList(data);
		List<Number> data2 = new ArrayList<Number>();
		boolean has = labelFunc != null;
		for (String xlabel : xlabels) {
			String name = has ? labelFunc.apply(xlabel) : xlabel;
			String name2;
			StatisValue statis = null;
			for (StatisValue values : values2) {
				name2 = has ? labelFunc.apply(values.getName()) : values.getName();
				if (name.equals(name2)) {
					statis = values;
					break;
				}
			}
			if (statis != null) {
				data2.add((Number)BeanUtils.getPrivateField(statis, attribute));
			} else {
				data2.add(0);
			}
		}
		echart.addYList(data2);
		
		return echart;
	}
	
	public static final EChartVO toChartVO(String legends,
					List<NameNumber> values1, List<NameNumber> values2,
					Function<String,String> labelFunc){
		int len = values1.size();
		List<String> xlabels = new ArrayList<String>(len);
		List<Number> data = new ArrayList<Number>();
		for (NameNumber statis : values1) {
			xlabels.add(statis.getName());
			data.add(statis.getNum());
		}
		EChartVO echart = new EChartVO(xlabels);
		if (!StringUtils.isEmpty(legends)) {
			echart.setLegend(Arrays.asList(legends.split(",")));
		} else {
			echart.setLegend(Lists.newArrayList("本期", "同期"));
		}
		echart.addYList(data);
		List<Number> data2 = new ArrayList<Number>();
		final boolean has = labelFunc != null;
		for (String xlabel : xlabels) {
			String name = has ? labelFunc.apply(xlabel) : xlabel;
			String name2;
			NameNumber statis = null;
			for (NameNumber values : values2) {
				name2 = has ? labelFunc.apply(values.getName()) : values.getName();
				if (name.equals(name2)) {
					statis = values;
					break;
				}
			}
			if (statis != null) {
				data2.add(statis.getNum());
			} else {
				data2.add(0);
			}
		}
		echart.addYList(data2);
		
		return echart;
	}
	
}
