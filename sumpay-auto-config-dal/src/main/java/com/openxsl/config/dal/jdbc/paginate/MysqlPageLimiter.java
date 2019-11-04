package com.openxsl.config.dal.jdbc.paginate;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.condition.ConditionalProperty;
import com.openxsl.config.dal.jdbc.PagedDaoTemplate.PageLimiter;

/**
 * Mysql分页
 * @author xiongsl
 */
@ScanConfig
@ConditionalProperty(name="jdbc.dialect", havingValue="mysql")
public class MysqlPageLimiter implements PageLimiter {

	@Override
	public String getPagedSql(String sql, int pageNo, int pageSize) {
		if (sql.toLowerCase().contains(" limit ")){
			return sql;
		}
		
		String limiter = String.format(" LIMIT %d, %d", pageNo*pageSize, pageSize);
		return new StringBuilder(sql).append(limiter).toString();
	}
	
	@Override
	public String existTable(String tableName) {
		String existSql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES "
				+ "where TABLE_NAME='%s'";
		return String.format(existSql, tableName);
	}

}
