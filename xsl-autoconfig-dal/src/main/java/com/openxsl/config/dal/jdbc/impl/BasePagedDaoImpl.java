package com.openxsl.config.dal.jdbc.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import com.alibaba.druid.pool.DruidDataSource;

import com.openxsl.config.dal.jdbc.PagedDaoTemplate;
import com.openxsl.config.rpcmodel.Page;
import com.openxsl.config.util.BeanUtils;

/**
 * 分页实现类
 * @author xiongsl
 *
 * @param <T>
 */
public abstract class BasePagedDaoImpl<T> implements PagedDaoTemplate<T>,
							ApplicationContextAware {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private ApplicationContext context;
	@Autowired
	protected PageLimiter pageLimiter;

	@Override
	public long getTotal(String sql, Object... args) {
		sql = String.format("SELECT count(1) as total FROM ( %s ) tmp", sql);
		Map<String,Object> resultMap = this.findMapBySql(sql, args);
		if (resultMap.size() > 0){
			Number total = (Number)resultMap.get("total");
			total = (total==null) ? (Number)resultMap.get("TOTAL") : total;
			return total.longValue();
		}else{
			return 0;
		}
	}
	
	@Override
	public Page<T> queryForPageBySql(String sql, int pageNo, int pageSize,
									Object... args) {
		int total = new Long(this.getTotal(sql, args)).intValue();
		sql = this.getPagedSql(sql, pageNo, pageSize);
		logger.debug("SQL: {}", sql);
		Page<T> page = new Page<T>(pageNo, pageSize, total);
		page.setResults(this.queryBySql(sql, args));
		return page;
	}
	
	public String getDialect() {
		String className = pageLimiter.getClass().getSimpleName();
		return className.substring(0, className.indexOf("PageLimiter"));
	}
	
	@Override
	public void createIfNotExist() {
		String sql = pageLimiter.existTable(this.getSqlParser().getTableName());
		if (!this.exists(sql)) {
			String createSql = this.getSqlParser().getCreateSql().trim();
			if ("Oracle".equals(this.getDialect())){
				createSql = createSql.replace("DATETIME", "DATE");
			}
			for (String str : createSql.split(";")) {
				this.executeSql(str.trim());
			}
		}
	}
	
	@Override
	public <R> List<R> queryBySql(String sql, Class<R> resultType, Object... args){
		List<R> lstResult = new ArrayList<R>();
		for (Map<String,Object> rowMap : this.queryMapBySql(sql, args)) {
			R rowObj = BeanUtils.instantiate(resultType);
			for (Field field : resultType.getDeclaredFields()) {
				String key = this.getSqlParser().getColumn(field);
				Object value = rowMap.get(key);
				if (value == null) {
					value = rowMap.get(key.toUpperCase());  //oracle
				}
				if (value != null) {
					if (value instanceof BigDecimal) {//jdbc.Number -> java.BigDecimal
						if (field.getType() == int.class) {  
							value = Integer.valueOf(value.toString());
						} else if (field.getType() == float.class) {
							value = Float.valueOf(value.toString());
						} else if (field.getType() == long.class) {
							value = Long.valueOf(value.toString());
						} else if (field.getType() == double.class) {
							value = Double.valueOf(value.toString());
						} else if (field.getType() == short.class) {
							value = Short.valueOf(value.toString());
						}
					}
					BeanUtils.setPrivateField(rowObj, field.getName(), value);
				}
			}
			lstResult.add(rowObj);
		}
		return lstResult;
	}
	
	@Override
	public String getPagedSql(String sql, int pageNo, int pageSize) {
		Assert.notNull(pageLimiter, "分页器为空");
		return pageLimiter.getPagedSql(sql, pageNo, pageSize);
	}
	
	@Override  //ApplicationContextAware
	public void setApplicationContext(ApplicationContext applicationContext)
					throws BeansException {
		context = applicationContext;
	}
	
	public void optimizeDataSource(String dataSourceName){
		DruidDataSource dataSource = (DruidDataSource)context.getBean(dataSourceName);
		int count = dataSource.getPoolingCount();
		dataSource.shrink(true);
		count -= dataSource.getPoolingCount();
		count += dataSource.removeAbandoned();
		logger.info("release {} connections", count);
	}
	
}
