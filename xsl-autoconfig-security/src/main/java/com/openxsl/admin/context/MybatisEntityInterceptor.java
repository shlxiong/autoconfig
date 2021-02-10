package com.openxsl.admin.context;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.util.BeanUtils;

/**
 * 处理修改时间
 * 注意：不要引入github.pageHelper这个包
 * 
 * @author shuilin.xiong
 */
@Intercepts({ 
	@Signature(type=ParameterHandler.class, method="setParameters", args={ PreparedStatement.class}),
	@Signature(type=Executor.class, method="update", args={ MappedStatement.class, Object.class }),
	@Signature(type=Executor.class, method="query", args={ MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }),
})
@Component
public class MybatisEntityInterceptor implements Interceptor {
	
	@Override
	public void setProperties(Properties properties) {
	}
	
    @Override    //in InterceptorChain
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object target = invocation.getTarget();
		if (target instanceof Executor) {
			return invokeExecutor(invocation);
		} else {
			return invokeSetParameter(invocation);
		}
	}
	
	//executor.update(mappedStatement, parameter)
	private Object invokeExecutor(Invocation invocation) throws Exception {
//		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
//		SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();   //获取 SQL 命令
        Object parameter = invocation.getArgs()[1];
        String method = invocation.getMethod().getName();
        if (parameter != null) {
			MappedStatement ms = (MappedStatement)invocation.getArgs()[0];
			String commandName = ms.getSqlCommandType().name();
        	if (commandName.toUpperCase().startsWith("INSERT") && parameter instanceof BaseEntity){
        		this.setCreateTime(parameter);
        		this.setUpdateTime(parameter);
			} else if ("update".equals(method)) {
	        	this.setUpdateTime(parameter);
        	} else {
        		this.setQueryGroup(parameter);
        	}
        }
        return invocation.proceed();
	}
	
	private Object invokeSetParameter(Invocation invocation) throws SQLException {
		ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
        PreparedStatement ps = (PreparedStatement) invocation.getArgs()[0];
        Object paramObj = parameterHandler.getParameterObject();
        Map<String, Object> paramMap = new MapperMethod.ParamMap<>();
        if (paramObj == null) {
            paramObj = paramMap;
        } else if (ClassUtils.isPrimitiveOrWrapper(paramObj.getClass())
                || String.class.isAssignableFrom(paramObj.getClass())
                || Number.class.isAssignableFrom(paramObj.getClass())) {
            paramMap.put("arg0", paramObj);
            paramObj = paramMap;
//        } else {
//            processParam(paramObj);
//        }
        }

        parameterHandler.setParameters(ps);
        return null;
	}
	
	private void setUpdateTime(Object parameter) {  //BaseEntity
		Date timenow = Calendar.getInstance().getTime();
		BeanUtils.setPrivateField(parameter, "modifyTime", timenow);
		if (null != LocalUserHolder.getUser()){
			BeanUtils.setPrivateField(parameter, "modifyBy", LocalUserHolder.getUserId());
		}
	}

	private void setCreateTime(Object parameter){
		Date timenow = Calendar.getInstance().getTime();
		BeanUtils.setPrivateField(parameter, "createTime", timenow);
		if (null != LocalUserHolder.getUser()){
			BeanUtils.setPrivateField(parameter, "createBy", LocalUserHolder.getUserId());
		}
	}
	
	private void setQueryGroup(Object parameter) {
		List<String> corpCodes = LocalUserHolder.getSubCorpCodes();
		if (parameter instanceof Map) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Map<String, Object> params = (Map) parameter;
			params.put("corpCodes", corpCodes);
//		} else if (parameter instanceof Params) {
//			Params params = (Params) parameter;
//			params.add("corpCodes", corpCodes);
		} else {
			BeanUtils.setPrivateField(parameter, "corpCodes", corpCodes);
		}
	}

}
