package com.openxsl.config.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
	
	/**
	 * 近12个月的起止时间
	 * @return
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
	
	public static int[] getLastYears10() {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		return new int[] {year-9, year};
	}
	/**
	 * 去年同期的起止时间
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
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

}
