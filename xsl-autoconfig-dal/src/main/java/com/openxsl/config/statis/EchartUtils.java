package com.openxsl.config.statis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.springframework.util.StringUtils;

import com.openxsl.config.util.BeanUtils;

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
	public static EChartVO toChartVO(List<? extends StatisValue> statisValues,
						String attributes, String legends){
		int len = statisValues.size();
		List<String> xlabels = new ArrayList<String>(len);
		for (StatisValue statis : statisValues) {
			xlabels.add(statis.getName());
		}
		EChartVO echart = new EChartVO(xlabels);
		if (!StringUtils.isEmpty(legends)) {
			echart.setLegend(Arrays.asList(legends.split(",")));
		}
		
		String[] fields = (StringUtils.isEmpty(attributes) ? "touristNum,totalPrice": attributes)
						.split(",");
		for (String field : fields) {
			List<Number> data = new ArrayList<Number>();
			for (StatisValue statis : statisValues) {
				Number num = (Number)BeanUtils.getPrivateField(statis, field);
				data.add(num==null ? 0 : num);
			}
			echart.addYList(data);
		}
		
		return echart;
	}
	
	public static final EChartVO toChartVO(List<? extends NameValue> values, String legends) {
		int len = values.size();
		List<String> xlabels = new ArrayList<String>(len);
		List<Number> data = new ArrayList<Number>(len);
		for (NameValue nameNum : values) {
			xlabels.add(nameNum.getName());
			data.add(nameNum.getNum());
		}
		EChartVO echart = new EChartVO(xlabels);
		echart.addYList(data);
		if (!StringUtils.isEmpty(legends)) {
			echart.setLegend(Arrays.asList(legends));
		}
		return echart;
	}
	
	//==================================== 本期同期 ======================================//
	/**
	 * 本期同期的双线图
	 * @param values  统计数据
	 * @param legends 图例名称(本期,同期)
	 */
	public static final EChartVO toChartVO2(List<NameNumber> values, String legends) {
		int len = values.size();
		List<String> xlabels = new ArrayList<String>(len);
		List<Number> data = new ArrayList<Number>(len);
		List<Number> last = new ArrayList<Number>(len);
		for (NameNumber nameNum : values) {
			xlabels.add(nameNum.getName());
			data.add(nameNum.getNum());
			last.add(nameNum.getLastNum());
		}
		EChartVO echart = new EChartVO(xlabels);
		echart.addYList(data);
		echart.addYList(last);
		if (StringUtils.isEmpty(legends)) {
			legends = "本期,同期";
		}
		echart.setLegend(Arrays.asList(legends.split(",")));
		return echart;
	}

	/**
	 * 两个序列组成一张图表，以序列1的X轴为准
	 * @param legends   图例名称(本期,同期)
	 * @param values1   序列1
	 * @param values2   序列2
	 * @param labelFunc 可选
	 */
	public static final EChartVO toChartVO2(String legends,
					List<NameNumber> values1, List<NameNumber> values2,
					Function<String,String> labelFunc) {
		if (labelFunc != null) {
			for (NameNumber values : values1) {
				values.setName(labelFunc.apply(values.getName()));
			}
			for (NameNumber values : values2) {
				values.setName(labelFunc.apply(values.getName()));
			}
		}
		NameNumber.mergeLastPeriods(values1, values2);
		
		return toChartVO2(values1, legends);
	}
	
	/**
	 * 转换为本期同期（月日）的双线图
	 * @param trendlist 本期和同期数据
	 * @param attribute 单个属性名
	 */
	public static final EChartVO toChartVO2(String attribute, List<Trendency> trendlist,
								String... legends){
		int len = trendlist.size();
		List<String> xlabels = new ArrayList<String>(len);
		List<Number> data1 = new ArrayList<Number>(len);
		List<Number> data2 = new ArrayList<Number>(len);
		for (Trendency trend : trendlist) {
			xlabels.add(trend.getName());
			Number num1 = (Number)BeanUtils.getPrivateField(trend.thisPeriod(), attribute);
			data1.add(num1==null ? 0 : num1);
			Number num2 = (Number)BeanUtils.getPrivateField(trend.lastPeriod(), attribute);
			data1.add(num2==null ? 0 : num2);
		}
		
		EChartVO echart = new EChartVO(xlabels);
		echart.addYList(data1);
		echart.addYList(data2);
		if (legends.length < 1 || StringUtils.isEmpty(legends[0])) {
			echart.setLegend(Arrays.asList("本期,同期".split(",")));
		} else {
			echart.setLegend(Arrays.asList(legends[0].split(",")));
		}
		return echart;
	}
	
	/**
	 * 两个共同属性的序列组成一张图表，以序列1的X轴为准
	 * @param legends   图例名称
	 * @param attribute 属性名
	 * @param values1   序列1
	 * @param values2   序列2
	 * @param labelFunc 可选
	 */
	public static final EChartVO toChartVO2(String legends, String attribute,
						List<StatisValue> values1, List<StatisValue> values2,
						Function<String,String> labelFunc){
		if (labelFunc != null) {
			for (StatisValue values : values1) {
				values.setName(labelFunc.apply(values.getName()));
			}
			for (StatisValue values : values2) {
				values.setName(labelFunc.apply(values.getName()));
			}
		}
		
		return toChartVO2(attribute, 
						Trendency.mergeLastPeriods(values1, values2), legends);
	}
	
	public static final EChartVO toChartVO3(List<NameNumber2> values, String legends) {
		int len = values.size();
		List<String> xlabels = new ArrayList<String>(len);
		List<Number> data = new ArrayList<Number>(len);
		List<Number> last = new ArrayList<Number>(len);
		List<Number> prev = new ArrayList<Number>(len);
		for (NameNumber2 nameNum : values) {
			xlabels.add(nameNum.getName());
			data.add(nameNum.getNum());
			prev.add(nameNum.getPrevNum());
			last.add(nameNum.getLastNum());
		}
		EChartVO echart = new EChartVO(xlabels);
		echart.addYList(data);
		echart.addYList(prev);
		echart.addYList(last);
		if (StringUtils.isEmpty(legends)) {
			legends = "本期,同期";
		}
		echart.setLegend(Arrays.asList(legends.split(",")));
		return echart;
	}
	
}
