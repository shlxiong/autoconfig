package com.openxsl.config.statis;

import java.sql.Date;
import java.text.ParseException;
import java.util.Calendar;

import com.openxsl.config.util.DateUtils;

public class TimeNowParams {
	private Date beginDate;
	private Date endDate;
	private Date monthBegin;
	private Date monthEnd;
	private Date yearBegin;
	private Date yearEnd;
	private Date weekBegin;
	private Date weekEnd;
	
	private Date lastWeekBegin;
	private Date lastWeekEnd;
	
	public TimeNowParams() {
		beginDate = endDate = new Date(System.currentTimeMillis());
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int weekNo = calendar.get(Calendar.WEEK_OF_YEAR);
		Date[] weekDays = DateUtils.getWeekDays(year, weekNo);
		weekBegin = weekDays[0];
		weekEnd = weekDays[1];
		weekDays = DateUtils.getWeekDays(year-1, weekNo);
		lastWeekBegin = weekDays[0];
		lastWeekEnd = weekDays[1];
		try {
			String[] strDates = StatisDateUtils.getThisMonth();
			monthBegin = DateUtils.parseSqlDate(strDates[0]);
			monthEnd = DateUtils.parseSqlDate(strDates[1]);
			strDates = StatisDateUtils.getThisYear();
			yearBegin = DateUtils.parseSqlDate(strDates[0]);
			yearEnd = DateUtils.parseSqlDate(strDates[1]);
		} catch (ParseException pe) {
			//nothing
		}
	}
	
	public Date getLastBegin() {
		return StatisDateUtils.getPrevDateOfLast(beginDate, 1);
	}
	public Date getLastEnd() {
		return StatisDateUtils.getPrevDateOfLast(endDate, 1);
	}
	public Date getLastWeekBegin() {
		return lastWeekBegin;
	}
	public Date getLastWeekEnd() {
		return lastWeekEnd;
	}
	public Date getLastMonthBegin() {
		return StatisDateUtils.getPrevDateOfLast(monthBegin, 1);
	}
	public Date getLastMonthEnd() {
		return StatisDateUtils.getPrevDateOfLast(monthEnd, 1);
	}
	
	public Date getPrevBegin() {
		return StatisDateUtils.getPrevDateOfLast(beginDate, 3);
	}
	public Date getPrevEnd() {
		return StatisDateUtils.getPrevDateOfLast(endDate, 3);
	}
	public Date getPrevWeekBegin() {
		return StatisDateUtils.getPrevDateOfLast(weekBegin, 4);
	}
	public Date getPrevWeekEnd() {
		return StatisDateUtils.getPrevDateOfLast(weekEnd, 4);
	}
	public Date getPrevMonthBegin() {
		return StatisDateUtils.getPrevDateOfLast(monthBegin, 2);
	}
	public Date getPrevMonthEnd() {
		return StatisDateUtils.getPrevDateOfLast(monthEnd, 2);
	}
	public Date getPrevYearBegin() {
		return StatisDateUtils.getPrevDateOfLast(yearBegin, 1);
	}
	public Date getPrevYearEnd() {
		return StatisDateUtils.getPrevDateOfLast(yearEnd, 1);
	}

	//=================================== JavaBean method==============================//
	public Date getBeginDate() {
		return beginDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public Date getMonthBegin() {
		return monthBegin;
	}
	public Date getMonthEnd() {
		return monthEnd;
	}
	public Date getYearBegin() {
		return yearBegin;
	}
	public void setYearBegin(Date yearBegin) {
		this.yearBegin = yearBegin;
	}
	public Date getYearEnd() {
		return yearEnd;
	}
	public Date getWeekBegin() {
		return weekBegin;
	}
	public Date getWeekEnd() {
		return weekEnd;
	}
	
}
