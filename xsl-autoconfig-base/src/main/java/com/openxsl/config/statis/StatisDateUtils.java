package com.openxsl.config.statis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.util.Assert;

/**
 * 统计类的日期函数(起止日期)
 * 
 * @author xiongsl
 */
public class StatisDateUtils {
	
	public static String[] getThisMonth() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE, 1);
		String beginDate = sdf.format(calendar.getTime());
		calendar.set(Calendar.DATE, calendar.getMaximum(Calendar.DATE));
		return new String[] {
				beginDate, sdf.format(calendar.getTime())
		};
	}
	
	public static String[] getThisYear() {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		return new String[] {
				year+"-01-01", year+"-12-31"
		};
	}
	
	public static String[] getLastDays30() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		String endDate = sdf.format(calendar.getTime());
		calendar.add(Calendar.DATE, -29);
		String beginDate = sdf.format(calendar.getTime());
		return new String[] {beginDate, endDate};
	}
	
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
	
	public static int[] getLastYears10() {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		return new int[] {year-9, year};
	}
	
	public static String[] getSamePeriod(String beginDate, String endDate) {
		String year1 = beginDate.substring(0, 4);
		String year2 = endDate.substring(0, 4);
		return new String[] {
				(Integer.parseInt(year1)-1) + beginDate.substring(4),
				(Integer.parseInt(year2)-1) + endDate.substring(4)
		};
	}
	
	public static String getFormatDate(Calendar calendar, String pattern) {
		return new SimpleDateFormat(pattern).format(calendar.getTime());
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
			} else {
				int s = Integer.valueOf(beginDate.split(" ")[1].split(":")[0]);
				int e = Integer.valueOf(endDate.split(" ")[1].split(":")[0]);
				if (type == 2) {
					List<String> years = new ArrayList<String>(e-s+1);
					for (int year=s; year<=e; year++) {
						years.add(String.valueOf(year));
					}
					return years;
				} else {
					return Arrays.asList(StatisDateUtils.getHourList()).subList(s, e);
				}
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

}
