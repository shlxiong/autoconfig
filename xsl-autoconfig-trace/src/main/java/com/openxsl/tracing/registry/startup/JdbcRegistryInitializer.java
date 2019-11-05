package com.openxsl.tracing.registry.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.alibaba.druid.pool.DruidDataSource;
import com.openxsl.config.tracing.service.RegistryService;
import com.openxsl.config.tracing.service.protocol.JdbcRegistry;
import com.openxsl.tracing.registry.model.JdbcRegInfo;

public class JdbcRegistryInitializer extends AbstractRegistryInitializer<DruidDataSource>{

	@Override
	public void onStartup(ServletContext sc) throws ServletException {
		System.out.println("Jdbc-TracingInitializer start, order=510");
		super.onStartup(sc);
	}
	
	@Override
	protected String getThreadName() {
		return "Jdbc-Registry";
	}
	
	@Override
	protected Class<? extends RegistryService> getServiceClass() {
		return JdbcRegistry.class;
	}

	@Override
	protected void register(DruidDataSource contextBean) {
		long start = System.currentTimeMillis();
		while (!contextBean.isInited()   //等待初始化完成
					&& (System.currentTimeMillis()-start)<MAX_WAIT_TIME) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		
		JdbcRegInfo registration = new JdbcRegInfo(contextBean.getUrl(),
							contextBean.getUsername());
		service.registerClient(registration);
	}

}
