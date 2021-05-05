package com.openxsl.config.statis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.util.DateUtils;

public class StatisDateUtils {
	
	/**
	 * 本月的开始日期和结束日期
	 */
	public static String[] getThisMonth() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		String yearMonth = sdf.format(new Date());
		String beginDate = yearMonth + "-01";
		int maxDay = Calendar.getInstance().get(Calendar.DATE);
		String endDate = yearMonth + "-" + maxDay;
		return new String[] { beginDate, endDate };
	}
	
	/**
	 * 本年的开始日期和结束日期
	 */
	public static String[] getThisYear() {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		return new String[] {
				year+"-01-01", year+"-12-31"
		};
	}
	
	/**
	 * 上个月的开始日期和结束日期
	 */
	public static String[] getLastMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		String yearMonth = sdf.format(calendar.getTime());
		String beginDate = yearMonth + "-01";
		int maxDay = calendar.get(Calendar.DATE);
		String endDate = yearMonth + "-" + maxDay;
		return new String[] { beginDate, endDate };
	}
	
	/**
	 * 近30天的开始日期和结束日期
	 */
	public static String[] getLastDays30() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		String endDate = sdf.format(calendar.getTime());
		calendar.add(Calendar.DATE, -29);
		String beginDate = sdf.format(calendar.getTime());
		return new String[] {beginDate, endDate};
	}
	
	/**
	 * 近12月的开始日期和结束日期
	 */
	public static String[] getLastMonths12() {
		return getLastMonths(12);
	}
	public static String[] getLastMonths(int months) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Calendar calendar = Calendar.getInstance();
		String endDate = sdf.format(calendar.getTime());
		calendar.add(Calendar.MONTH, 1-months);
		String beginDate = sdf.format(calendar.getTime());
		return new String[] {beginDate, endDate};
	}
	
	/**
	 * 近10年的开始日期和结束日期
	 */
	public static int[] getLastYears10() {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		return new int[] {year-9, year};
	}
	
	/**
	 * 某个日期的“环比”日(type=1为同比)
	 * @param thisDate
	 * @param type 类型：1-上一年，2-上个月，3-前一日，4-上周，5-上个季度
	 */
	public static java.sql.Date getPrevDateOfLast(java.sql.Date thisDate, int type){
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(true);
		calendar.setTime(thisDate);
		int offset = -1, field = Calendar.DATE;
		if (type == 1) {
			field = Calendar.YEAR;
		} else if (type == 2) {
			field = Calendar.MONTH;
		} else if (type == 4) {
			offset = -7;
		} else if (type == 5) {
			field = Calendar.MONTH;
			offset = -3;
		} 
		
		calendar.add(field, offset);
		return new java.sql.Date(calendar.getTime().getTime());
	}
	
	/**
	 * 调用方法，得到本期同期的统计值
	 * @param action   业务方法
	 * @param params   参数
	 * @param periods  本期同期
	 * @param fillType 类型 (0:yyyy-MM-dd; 1:yyyy-MM; 2:yyyy; 3:HH)
	 */
	public static <T extends QueryMap<Object>> List<NameNumber> execute(
						Function<T,List<NameNumber>> action, T params, 
						ComparisonPeriods periods, int fillType) {
		params.put("beginDate", periods.getBeginDate());
		params.put("endDate", periods.getEndDate());
		List<NameNumber> thisPeriod = action.apply(params);
		params.put("beginDate", periods.getLastBegin());
		params.put("endDate", periods.getLastEnd());
		List<NameNumber> lastPeriod = action.apply(params);
		
		if (fillType < 0 || fillType > 1) {
			NameNumber.mergeLastPeriods(thisPeriod, lastPeriod);
		} else {
			NameNumber.mergeLastPeriods(thisPeriod, lastPeriod, s->s.substring(5));
		}
		if (fillType != -1) {
			String strDate1 = periods.getBeginDate().toString();
			String strDate2 = periods.getEndDate().toString();
			refillDates(strDate1, strDate2, fillType, thisPeriod, true);
		}
		return new NameNumList(thisPeriod);
	}
	/**
	 * 调用方法，得到本期同期的统计值
	 * @param action   业务方法
	 * @param params   参数
	 * @param periods  本期同期
	 * @param fillType 类型 (0:yyyy-MM-dd; 1:yyyy-MM; 2:yyyy; 3:HH)
	 */
	public static <T extends QueryMap<Object>> List<Trendency> execute2(
						Function<T,List<StatisValue>> action, T params, 
						ComparisonPeriods periods, int fillType) {
		params.put("beginDate", periods.getBeginDate());
		params.put("endDate", periods.getEndDate());
		List<StatisValue> thisPeriod = action.apply(params);
		params.put("beginDate", periods.getLastBegin());
		params.put("endDate", periods.getLastEnd());
		List<StatisValue> lastPeriod = action.apply(params);
		
		List<Trendency> results = Trendency.mergeLastPeriods(thisPeriod, lastPeriod);
		if (fillType != -1) {
			String strDate1 = periods.getBeginDate().toString();
			String strDate2 = periods.getEndDate().toString();
			refillDates(strDate1, strDate2, fillType, results, false);
		}
		return results;
	}
	
	public static <T extends QueryMap<Object>> Trendency executeTotal(
						Function<T,StatisValue> action, T params, 
						ComparisonPeriods periods) {
		params.put("beginDate", periods.getBeginDate());
		params.put("endDate", periods.getEndDate());
		StatisValue thisPeriod = action.apply(params);
		params.put("beginDate", periods.getLastBegin());
		params.put("endDate", periods.getLastEnd());
		StatisValue lastPeriod = action.apply(params);
		return new Trendency(thisPeriod, lastPeriod);
	}
	
	public static String getFormatDate(Calendar calendar, String pattern) {
		return new SimpleDateFormat(pattern).format(calendar.getTime());
	}
	
	/**
	 * 填充统计值列表中不存在的日期/时段
	 * @param beginDate 开始日期(yyyy-MM-dd)
	 * @param endDate   结束日期(yyyy-MM-dd)
	 * @param type    类型 (0:yyyy-MM-dd; 1:yyyy-MM; 2:yyyy; 3:HH)
	 * @param values  统计值
	 * @param simple  true?NameNumber:StatisValue
	 */
	@SuppressWarnings("unchecked")
	public static void refillDates(String beginDate, String endDate, int type,
				List<? extends NameValue> values, Boolean... simple) {
		if (values == null) {
			values = new ArrayList<NameValue>();
		}
		List<String> elements = new TimeIterator(beginDate, endDate).setType(type)
								.elements();
		if (elements.size() > 365) {
			throw new IllegalArgumentException("时段区间不能大于365天");
		}
		Set<String> nameSet = values.stream().map(NameValue::getName)
								.collect(Collectors.toSet());
		if (type < 2) {//去掉年份'yyyy-'
			elements = elements.stream().map(e->e.substring(5)).collect(Collectors.toList());
			nameSet = nameSet.stream().map(e->e.substring(5)).collect(Collectors.toSet());
		}
		
		boolean flag = simple.length > 0 ? simple[0].booleanValue() : true;
		int idx = 0;
		for (String name : elements) {
			if (!nameSet.contains(name)) {
				if (flag) {
					((List<NameNumber>)values).add(idx, new NameNumber().setName(name));
				} else {
					((List<StatisValue>)values).add(idx, new StatisValue().setName(name));
				}
			} else {
				if (flag) {
					((List<NameNumber>)values).get(idx).setName(name);
				} else {
					((List<StatisValue>)values).get(idx).setName(name);
				}
			}
			idx ++;
		}
	}

	/**
	 * 获取小时集合
	 */
	public static String[] getHourList() {
		return new String[]{
				"00", "01", "02", "03", "04", "05", "06", "07",
				"08", "09", "10", "11", "12", "13", "14", "15",
				"16", "17", "18", "19", "20", "21", "22", "23"
			};
	}
	
	public static List<String> listDays(String beginDate, String endDate) {
		List<String> list = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(sdf.parse(beginDate));
			final Date date2 = sdf.parse(endDate);
			while (calendar.getTime().compareTo(date2) <= 0) {
				list.add(sdf.format(calendar.getTime()));
				calendar.add(Calendar.DATE, 1);
			}
		} catch (ParseException e) {
			throw new IllegalArgumentException("the dateformat must be 'yyyy-MM-dd'");
		}
		return list;
	}
	
	public static List<String> listMonths(String beginDate, String endDate) {
		List<String> list = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(sdf.parse(beginDate));
			final Date date2 = sdf.parse(endDate);
			while (calendar.getTime().compareTo(date2) <= 0) {
				list.add(sdf.format(calendar.getTime()));
				calendar.add(Calendar.MONTH, 1);
			}
		} catch (ParseException e) {
			throw new IllegalArgumentException("the dateformat must be 'yyyy-MM'");
		}
		return list;
	}
	
	public static class TimeIterator {
		private String beginDate;
		private String endDate;
		private int type;   //0:yyyy-MM-dd; 1:yyyy-MM; 2:yyyy; 3:HH
		
		public TimeIterator() {}
		public TimeIterator(int type) {
			this.setType(type);
		}
		public TimeIterator(String beginDate, String endDate) {
			this.setBeginDate(beginDate);
			this.setEndDate(endDate);
		}
		
		public List<String> elements(){
			Assert.isTrue(beginDate!=null && endDate!=null, "");
			if (type == 0) {
				return StatisDateUtils.listDays(beginDate, endDate);
			} else if (type == 1) {
				return StatisDateUtils.listMonths(beginDate, endDate);
			} else if (type == 3){  //HH
				return Arrays.asList(StatisDateUtils.getHourList());
			} else {
				int s, e;
				try {
					s = Integer.valueOf(beginDate);
					e = Integer.valueOf(endDate);
				} catch (NumberFormatException ne) { //日期格式
					s = Integer.valueOf(beginDate.substring(0, 4));
					e = Integer.valueOf(endDate.substring(0, 4));
				}
				List<String> years = new ArrayList<String>(e-s+1);
				for (int year=s; year<=e; year++) {
					years.add(String.valueOf(year));
				}
				return years;
			}
		}
		
		public String getBeginDate() {
			return beginDate;
		}
		public void setBeginDate(String beginDate) {
			this.beginDate = beginDate;
		}
		public String getEndDate() {
			return endDate;
		}
		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}
		public int getType() {
			return type;
		}
		public TimeIterator setType(int type) {
			this.type = type;
			return this;
		}
		
	}
	
	public static class ComparisonPeriods {
		private java.sql.Date beginDate;
		private java.sql.Date endDate;
		private java.sql.Date lastBegin;
		private java.sql.Date lastEnd;
		private int defaultType = 1;   //同比
		
		Logger logger = LoggerFactory.getLogger(getClass());
		
		public ComparisonPeriods() {}
		public ComparisonPeriods(java.sql.Date beginDate, java.sql.Date endDate) {
			this.beginDate = beginDate;
			this.endDate = endDate;
		}
		public ComparisonPeriods(String strBegin, String strEnd) {
			try {
				beginDate = DateUtils.parseSqlDate(strBegin);
				endDate = DateUtils.parseSqlDate(strEnd);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		
		public void setDefaultType(int type) {
			this.defaultType = type;
		}
		
		public java.sql.Date getBeginDate() {
			return beginDate;
		}
		public void setBeginDate(java.sql.Date beginDate) {
			this.beginDate = beginDate;
		}
		public java.sql.Date getEndDate() {
			return endDate;
		}
		public void setEndDate(java.sql.Date endDate) {
			this.endDate = endDate;
		}
		public java.sql.Date getLastBegin() {
			return lastBegin!=null ? lastBegin : getPrevDateOfLast(beginDate, defaultType);
		}
		public void setLastBegin(java.sql.Date lastBegin) {
			this.lastBegin = lastBegin;
		}
		public java.sql.Date getLastEnd() {
			return lastEnd!=null ? lastEnd : getPrevDateOfLast(endDate, defaultType);
		}
		public void setLastEnd(java.sql.Date lastEnd) {
			this.lastEnd = lastEnd;
		}
		
	}
}
