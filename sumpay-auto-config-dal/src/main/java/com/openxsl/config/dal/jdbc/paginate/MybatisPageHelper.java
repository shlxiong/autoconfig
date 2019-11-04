package com.openxsl.config.dal.jdbc.paginate;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openxsl.config.dal.jdbc.PagedDaoTemplate.PageLimiter;
import com.openxsl.config.rpcmodel.Page;

/**
 * <p>
 * Mybatis的分页插件，做分页查询时，需在Mapper接口的方法中，有一个名为"page",
 * 或类型为com.openxsl.config.rpcmodel.Page的参数
 * </p>
 * @author 001327-xiongsl
 * @lastModified 2018-07-06
 */
@SuppressWarnings("rawtypes")
@Intercepts(@Signature(type=StatementHandler.class, method="prepare", args={ Connection.class, Integer.class }))
public class MybatisPageHelper implements Interceptor {
	private static final Logger logger = LoggerFactory.getLogger(MybatisPageHelper.class);
	private final String PARAM_PAGE = "page";
	private PageLimiter limiter = new MysqlPageLimiter();
	
	@Override    //in InterceptorChain
	public Object plugin(Object target) {
		if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
	}

	/**
	 * 代理执行：Statement StatementHandler.prepare(Connection);
	 */
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler target = (StatementHandler)invocation.getTarget();  //target=RoutingStatementHandler
		BoundSql boundSql = target.getBoundSql();
		MetaObject metaObject = this.getMetaObject(target);
		//target.delegate=PreparedStatementHandler
		if (boundSql == null){
			boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
		}
		if (boundSql.getSql().startsWith("SELECT")){
//			RowBounds rowBounds = (RowBounds)metaObject.getValue("delegate.rowBounds");
			Page page = this.getPageParam(boundSql);
			if (page != null) {
				String sql = boundSql.getSql();
				int pageNo = page.getPageNo();      //rowBounds.getOffset(), 
				int pageSize = page.getPageSize();  //rowBounds.getLimit()
				sql = limiter.getPagedSql(sql, pageNo, pageSize);
				logger.info("分页语句: {}", sql);
				metaObject.setValue("delegate.boundSql.sql", sql);
			}
		}
		return invocation.proceed();
	}

	@Override
	public void setProperties(Properties properties) {
		String dialect = System.getProperty("jdbc.dialect"); //properties.getProperty("dialect")
		if (dialect != null) {
			this.setDialect(dialect);
		}
	}
	
	private Page getPageParam(BoundSql boundSql) {
		Object paramObject = boundSql.getParameterObject();
		if (paramObject == null) {
			return null;
		}
		if (paramObject instanceof Page) {
			return (Page)paramObject;
		} else if (paramObject instanceof ParamMap) {
			MapperMethod.ParamMap<?> paramMap = (MapperMethod.ParamMap)paramObject;
			if (paramMap.containsKey(PARAM_PAGE)) {
				return (Page)paramMap.get(PARAM_PAGE);
			} else {
				for (Object value : paramMap.values()) {
					if (value!=null && value instanceof Page) {
						return (Page)value;
					}
				}
			}
		}
		
		return null;
	}

	public void setDialect(String dialect){
		logger.info("use database 【{}】", dialect);
		if ("oracle".equals(dialect)) {
			this.limiter = new OraclePageLimiter();
		} 
		//default as 'mysql'
	}
	
	/**
	 * 分离代理对象链(由于目标类可能有拦截器，从而形成多次代理，通过下面的两次循环 可以分离出最原始的的目标类)
	 */
	private MetaObject getMetaObject(StatementHandler target){
		MetaObject metaObject = SystemMetaObject.forObject(target);
		while (metaObject.hasGetter("h")){
			Object object = metaObject.getValue("h");
			metaObject = SystemMetaObject.forObject(object);
		}
		while (metaObject.hasGetter("target")){
			Object object = metaObject.getValue("target");
			metaObject = SystemMetaObject.forObject(object); 
		}
		return metaObject;
	}

}
