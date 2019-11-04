package com.openxsl.config.boot;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import com.openxsl.config.BootstrapApplication;
import com.openxsl.config.Environment;
import com.openxsl.config.autodetect.AutoConfigRegistryPostProcessor;
import com.openxsl.config.loader.DomainPropertyLoadSpi;
import com.openxsl.config.tracing.registry.service.ComponentsRegistryService;
import com.openxsl.config.util.SpringRegistry;

/**
 * Spring-boot启动类：
 * 		增加"cn.sumpay.config"扫描包
 *  	接管配置文件和日志管理
 * @author xiongsl
 */
public class OpenxslApplication extends SpringApplication{
	
	public static ConfigurableApplicationContext run(Object sources, String[] args) {
		//sources.add  SumpayApplication.class.getPackage();  @Component
		return new OpenxslApplication(sources).run(args);
	}
	public static ConfigurableApplicationContext run(Object[] sources, String[] args) {
		return new OpenxslApplication(sources).run(args);
	}
	
	public OpenxslApplication(Object... sources) {
		super(sources);
		this.setEnvironment();
	}
	
//	protected ConfigurableApplicationContext createApplicationContext() {
//		return Environment.getSpringContext();  //TODO ?
//	}
	
	@Override
	protected void refresh(ApplicationContext applicationContext) {
		ConfigurableApplicationContext context = (ConfigurableApplicationContext)applicationContext;
		Properties props = DomainPropertyLoadSpi.loadProperties();
		if (props.size() > 0) {
			SpringRegistry.addPlaceholderConfigurer(props, context);
		}
		context.addBeanFactoryPostProcessor(
				new AutoConfigRegistryPostProcessor(Environment.getSpringEnvironment()));

		super.refresh(applicationContext);
		ComponentsRegistryService.register(context);
	}
	
//	protected void afterRefresh(ConfigurableApplicationContext context,
//								ApplicationArguments args) {
//		super.afterRefresh(context, args);
//	}
	
	private void setEnvironment() {
		super.setLogStartupInfo(false);
		super.setAddCommandLineProperties(false);
		super.setBannerMode(Mode.LOG);
		
		this.removeConfigLoggingListener();
		BootstrapApplication.initLogging();
		super.setEnvironment(Environment.getSpringEnvironment());
		
		if (Environment.exists("com.alibaba.druid.pool.DruidDataSource")) {  //druid-beta
			super.getSources().add("classpath*:/spring/dal/http-client.xml");
		}
	}
	private void removeConfigLoggingListener() {
		LinkedHashSet<ApplicationListener<?>> listeners = new LinkedHashSet<ApplicationListener<?>>();
		Iterator<ApplicationListener<?>> iter = this.getListeners().iterator();
		while (iter.hasNext()) {
			ApplicationListener<?> listener = iter.next();
			if (listener instanceof ConfigFileApplicationListener
					|| listener instanceof LoggingApplicationListener) {
				listeners.remove(listener);
				continue;
			} else {
				listeners.add(listener);
			}
		}
		this.setListeners(listeners);
	}

}
