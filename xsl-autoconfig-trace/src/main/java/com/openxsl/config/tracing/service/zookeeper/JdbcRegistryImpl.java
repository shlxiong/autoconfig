package com.openxsl.config.tracing.service.zookeeper;

import java.sql.SQLException;

import com.alibaba.druid.pool.DruidDataSource;
import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.tracing.service.protocol.JdbcRegistry;
import com.openxsl.tracing.registry.model.JdbcRegInfo;
import com.openxsl.tracing.registry.model.Registration;

@ScanConfig
public class JdbcRegistryImpl extends BaseZkRegistry implements JdbcRegistry {

	@Override
	protected String getNamespace() {
		return NAMESPACE;
	}
	
	@Override
	public Class<?> getContextBeanType() {
		return DruidDataSource.class;
	}

	@Override
	public Registration convertRegistra(Object bean) {
		DruidDataSource dataSource = (DruidDataSource)bean;
		try {
			//springboot没有初始化， if (!dataSource.isInited()){
			dataSource.init();  //方法内会判断
		} catch (SQLException e) {
			throw new IllegalStateException("Can't init DruidDataSource, please check config-items");
		}
		return new JdbcRegInfo(dataSource.getUrl(),	dataSource.getUsername());
	}

}
