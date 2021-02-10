package com.openxsl.config.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.util.Assert;

/**
 * 日期操作类
 * @author xiongsl
 */
public final class DateUtils {
	public static final String DEF_FORMAT_DATE = "yyyy-MM-dd";
	public static final String DEF_FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * 日期格式化。如果format为空，则使用默认的日期格式
	 */
	public static String format(Date date, String...format) {
		Assert.notNull(date, "Date对象为空");
		if (format.length == 0 || StringUtils.isEmpty(format[0])) {
			if (date instanceof java.sql.Date) {
				return new SimpleDateFormat(DEF_FORMAT_DATE).format(date);
			} else {
				return new SimpleDateFormat(DEF_FORMAT_DATETIME).format(date);
			}
		} else {
			return new SimpleDateFormat(format[0]).format(date);
		}
	}
	/**
	 * 格式化日期
	 * @param date 日期对象
	 * @param type 日期类型：0:yyyy, 1:yyyy-MM, 2:yyyy-MM-dd, 3:yyyy-MM-dd HH, 4:yyyy-MM-dd HH:mm, 5:yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String format(Date date, int type) {
		return getDateFormat(type).format(date);
	}
	
	public static String getCurrentDate(String pattern) {
		if (pattern == null) {
			pattern = DEF_FORMAT_DATETIME;
		}
		return new SimpleDateFormat(pattern).format(new Date());
	}
	public static Calendar getCalendar(int year, int month, int date, int hour, int minute, int sec) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, date, hour, minute, sec);
		return calendar;
	}
	/**
	 * 将制定格式的串转换为日期对象。如果format为空，则尝试匹配默认的日期格式
	 */
	public static Date parse(String strDate, String... format) throws ParseException{
		Assert.notNull(strDate, "Date字符串为空");
		if (format.length == 0 || StringUtils.isEmpty(format[0])) {
			try {
				return new SimpleDateFormat(DEF_FORMAT_DATETIME).parse(strDate);
			} catch (ParseException pe) {
				return new SimpleDateFormat(DEF_FORMAT_DATE).parse(strDate);
			}
		} else {
			return new SimpleDateFormat(format[0]).parse(strDate);
		}
	}
	/**
	 * 字符转换为日期对象
	 * @param dateStr 格式化的日期字符
	 * @param type 日期类型：0:yyyy, 1:yyyy-MM, 2:yyyy-MM-dd, 3:yyyy-MM-dd HH, 4:yyyy-MM-dd HH:mm, 5:yyyy-MM-dd HH:mm:ss
	 */
	public static Date parse(String dateStr, int type) throws ParseException{
		return getDateFormat(type).parse(dateStr);
	}
	public static java.sql.Date parseSqlDate(String dateStr) throws ParseException{
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
		return new java.sql.Date(date.getTime());
	}
	public static java.sql.Date transferSqlDate(Date date){
		return new java.sql.Date(date.getTime());
	}
	
	/**
	 * 返回距离明日凌晨的毫秒数
	 */
	public static long getMilSecBeforeTomorrow() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime().getTime() - System.currentTimeMillis();
	}
	
	public static int getDaysBeforeNextMonth() {
		Calendar calendar = Calendar.getInstance();
		int days = calendar.get(Calendar.DATE);
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DATE, 1);
		return calendar.get(Calendar.DATE) - days - 1;
	}
	
	public static long getIntervals(Date date1, Date date2, int unit) {
		Assert.notNull(date1, "Date对象为空");
		long ms = date2.getTime() - date1.getTime();
		switch (unit) {
			case Calendar.SECOND:
				ms = ms / 1000;
				break;
			case Calendar.MINUTE:
				ms = ms / 1000 / 60;
				break;
			case Calendar.HOUR:
				ms = ms / 1000 / 3600;
				break;
			case Calendar.DATE:
				ms = ms / 1000 / 3600 / 24;
				break;
			case Calendar.MONTH:
				Calendar calendar1 = Calendar.getInstance();
				calendar1.setTime(date1);
				Calendar calendar2 = Calendar.getInstance();
				calendar2.setTime(date2);
				int years = calendar2.get(Calendar.YEAR) - calendar1.get(Calendar.YEAR);
				int months = calendar2.get(Calendar.MONTH) - calendar1.get(Calendar.MONTH);
				ms = years * 12 + months;
				break;
			default:
		}
		return ms;
	}
	
	/**
	 * 增加日期的某一个值（0-年、1-月、2-日、3-时、4-分、5-秒）
	 * @param dateStr 格式化的日期字符
	 * @param field 日期属性
	 * @param interval 增加值
	 * @throws ParseException
	 */
	public static String addDateTime(String dateStr, int field, int interval) {
		Calendar calendar = Calendar.getInstance();
		Date theDate = null;
		int dateType = 5;
		while (theDate==null && dateType>=0) {
			try {
				theDate = parse(dateStr, dateType);
			} catch (ParseException e) {
				dateType--;
			}
		}
		calendar.setTime(theDate);
		
		switch (field) {
			case 0:  calendar.add(Calendar.YEAR, interval); break;
			case 1:  calendar.add(Calendar.MONTH, interval); break;
			case 2:  calendar.add(Calendar.DATE, interval); break;
			case 3:  calendar.add(Calendar.HOUR_OF_DAY, interval); break;
			case 4:  calendar.add(Calendar.MINUTE, interval); break;
			default: calendar.add(Calendar.SECOND, interval);
		}
		
		return format(calendar.getTime(), dateType);
	}
	
	public static java.sql.Date[] getWeekDays(int year, int weekNo) {
		Calendar calendar = Calendar.getInstance();
        calendar.set(year, 0, 1);
        int dayOfWeek = 7 - calendar.get(Calendar.DAY_OF_WEEK) + 1; //计算出第一周还剩几天
        int days = (weekNo-2) * 7;   //间隔的完整周
        calendar.add(Calendar.DAY_OF_YEAR, days + dayOfWeek);
        Date monDay = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 6);
        Date sunDay = calendar.getTime();
        return new java.sql.Date[] {
        		transferSqlDate(monDay), transferSqlDate(sunDay)
        };
	}
	
	private static SimpleDateFormat getDateFormat(int type) {
		switch (type) {
			case 0:  return new SimpleDateFormat("yyyy");
			case 1:  return new SimpleDateFormat("yyyy-MM");
			case 2:  return new SimpleDateFormat("yyyy-MM-dd");
			case 3:  return new SimpleDateFormat("yyyy-MM-dd HH");
			case 4:  return new SimpleDateFormat("yyyy-MM-dd HH:mm");
			default: return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		} 
	}
	
}
