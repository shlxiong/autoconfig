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
	
}
