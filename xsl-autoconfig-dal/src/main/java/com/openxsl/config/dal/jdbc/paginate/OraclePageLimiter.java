package com.openxsl.config.dal.jdbc.paginate;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.condition.ConditionalProperty;
import com.openxsl.config.dal.jdbc.PagedDaoTemplate.PageLimiter;

/**
 * Oracle分页
 * @author xiongsl
 */
@ScanConfig
@ConditionalProperty(name="jdbc.dialect", havingValue="oracle")
public class OraclePageLimiter implements PageLimiter {

	/**
	 * SELECT * FROM
(
   SELECT A.*, ROWNUM RN
   FROM (SELECT * FROM TABLE_NAME) A
   WHERE ROWNUM <= 40
)
WHERE RN >= 21
	 */
	@Override
	public String getPagedSql(String sql, int pageNo, int pageSize) {
		StringBuilder buffer = new StringBuilder(sql.length());
		buffer.append("SELECT * FROM (")
			.append("SELECT A.*, ROWNUM RN FROM ( ").append(sql).append(" ) A ")
			.append("WHERE ROWNUM <= ").append((pageNo+1)*pageSize)
			.append(") B WHERE RN > ").append(pageNo*pageSize);
		return buffer.toString();
	}
	
	@Override
	public String existTable(String tableName) {
		//当前用户下存在某张表
		String existSql = "select table_name from user_tables where table_name = '%s'";
		return String.format(existSql, tableName.toUpperCase());
	}

}
