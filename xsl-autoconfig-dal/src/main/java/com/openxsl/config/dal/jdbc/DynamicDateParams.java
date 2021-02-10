package com.openxsl.config.dal.jdbc;

import java.sql.Date;
import java.util.List;

import org.springframework.util.Assert;

import com.openxsl.config.rpcmodel.QueryMap;

/**
 * 公共的查询条件，包括：{@link #getBeginDate()}，{@link #getEndDate()}，{@link #getScenicCode()}，{@link #getCorpCodes()}等
 * 
 * @author shuilin.xiong
 */
@SuppressWarnings("serial")
public class DynamicDateParams extends QueryMap<Object> {
	protected final String BETWEEN_DATE = "statis_date between :beginDate and :endDate";
	protected final String BETWEEN_DATE_STR = "statis_date between str_to_date(:beginDate,'%Y-%m-%d')"
			+ " and str_to_date(:endDate,'%Y-%m-%d')";
	final String CORPS_WHERE = "    AND corp_code in (:corpCodes)";
	final String SCENIC_WHERE = "    AND scenic_code = :scenicCode";
	
	public DynamicDateParams(String business) {
		this.setBusiness(business);
	}
	
	public String wrapSql(String sql) {
		sql = sql.replace("TABLE_NAME", this.getBusiness());
		StringBuilder buffer = new StringBuilder(sql);
		Assert.notNull(this.getBeginDate(), "查询日期不能为空");
		if (this.getBeginDate() instanceof Date) {
			this.appendWhere(buffer, BETWEEN_DATE);
		} else {
			this.appendWhere(buffer, BETWEEN_DATE_STR);
		}
		if (this.getScenicCode() != null) {
			this.appendWhere(buffer, SCENIC_WHERE);
		}
		if (this.getCorpCodes() != null && this.getCorpCodes().size() > 0) {
			this.appendWhere(buffer, CORPS_WHERE);
		}
		return buffer.toString();
	}
	protected void appendWhere(StringBuilder buffSql, String where) {
		int idx = Math.max(buffSql.indexOf(" ORDER BY "), buffSql.indexOf(" order by "));
		if (idx == -1) {
			idx = buffSql.length();
		}
		buffSql.insert(idx, where);
	}
	
	public String getBusiness() {
		return (String)this.get("business");
	}
	public void setBusiness(String tableName) {
		this.put("business", tableName);
	}
	public Object getBeginDate() {
		return this.get("beginDate");
	}
	public void setBeginDate(Object beginDate) {
		this.put("beginDate", beginDate);
	}
	public Object getEndDate() {
		return this.get("endDate");
	}
	public void setEndDate(Object tableName) {
		this.put("endDate", tableName);
	}
	public void setScenicCode(String scenicCode) {
		this.put("scenicCode", scenicCode);
	}
	public String getScenicCode() {
		return (String)this.get("scenicCode");
	}
	public void setCorpCodes(List<String> corpCodes) {
		if (corpCodes != null && corpCodes.size() > 0) {
			this.put("corpCodes", corpCodes);
		}
	}
	@SuppressWarnings("unchecked")
	public List<String> getCorpCodes(){
		return (List<String>)this.get("corpCodes");
	}

}
