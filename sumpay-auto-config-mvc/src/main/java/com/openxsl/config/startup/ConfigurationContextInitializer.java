package com.openxsl.config.startup;

import java.util.EventListener;
import java.util.Properties;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

//import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

import com.alibaba.fastjson.JSON;

import com.openxsl.config.BootstrapApplication;
import com.openxsl.config.Environment;
import com.openxsl.config.loader.MvcPropertiesLoader;
import com.openxsl.config.loader.OtherPropertiesLoader;
import com.openxsl.config.util.StringUtils;
import com.openxsl.config.util.Version;
import freemarker.template.Configuration;

/**
 * 初始化SpringMvc、CharacterEncodingFilter
 * @author xiongsl
 */
@Order(1)
public class ConfigurationContextInitializer implements WebApplicationInitializer {
	private String[] requestSuffix;

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		if (Environment.existSpringBoot()) {
			return;
		}
		System.out.println("ConfigurationContextInitializer start, order=1");
		
		Version.expectVersion(ApplicationContext.class, "4.3.7.RELEASE", true);
		Version.expectVersion(JSON.class, "1.2.28", true);
		Version.expectVersionIfExist("com.google.gson.Gson", "2.8.2", true);
		Version.expectVersion(Configuration.class, "2.3.23", true);
		Version.expectVersionIfExist("org.mybatis.spring.SqlSessionTemplate", "1.3.1", true);
		Version.expectVersionIfExist("com.alibaba.druid.pool.DruidDataSource", "1.1.5", true);
		
		this.setInitialParams(servletContext);
		this.addContextListeners(servletContext);  //auto-activate
		this.addFilters(servletContext);
		this.addServlets(servletContext);
	}
	
	private void setInitialParams(ServletContext servletContext) {
		String suffix = Environment.getProperty("spring.mvc.request.suffix", "*.htm");
		requestSuffix = StringUtils.split(suffix, ",");
		Environment.getSpringContext().addApplicationListener(new TracingBootApplicationListener());
		String rootContext = 
					BootstrapApplication.initEnvironment(Environment.getSpringContext());
		System.out.println("    spring context locations: " + rootContext);
		
		servletContext.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, rootContext);
//		servletContext.setInitParameter(ContextLoader.CONTEXT_INITIALIZER_CLASSES_PARAM,
//							ContextComponentScanner.class.getName());
	}
	
	@SuppressWarnings("unchecked")
	private void addContextListeners(ServletContext servletContext) {
		Environment.setSpringContextLoader();
		servletContext.addListener(new ContextLoaderListener(Environment.getSpringContext()));
		
		String listeners = Environment.getProperty("servlet.listeners");
		if (!StringUtils.isEmpty(listeners)) {
			for (String listener : StringUtils.split(listeners,",")) {
				try {
					Class<EventListener> clazz = (Class<EventListener>)
							ClassUtils.forName(listener,  ClassUtils.getDefaultClassLoader());
					System.out.println("    Find servlet.listener: "+listener);
					servletContext.addListener(clazz);
				}catch(ClassNotFoundException cnfe) {
					System.err.println("    current context not exists class: " + listener);
				}catch (ClassCastException cce) {
					System.err.println(listener + " is not instanceof java.util.EventListener");
				}
			}
		}
	}
	
	private void addFilters(ServletContext servletContext) {
		FilterRegistration.Dynamic encoding = servletContext.addFilter(
					"encoding", "org.springframework.web.filter.CharacterEncodingFilter");
		if (encoding != null) {
			encoding.setInitParameter("encoding", "utf-8");
			encoding.setInitParameter("forceEncoding", "true");
			encoding.addMappingForUrlPatterns(null, true, requestSuffix);
			encoding.addMappingForUrlPatterns(null, true, "*.jsp","*.html");
		}
		servletContext.addFilter("httpMethodFilter",
					"org.springframework.web.filter.HiddenHttpMethodFilter");
	}
	
	private void addServlets(ServletContext servletContext) {
		String servletClass = "com.alibaba.druid.support.http.StatViewServlet";
		if (Environment.exists(servletClass)) {
			ServletRegistration.Dynamic druid = servletContext.addServlet("DruidStatView", servletClass);
			if (druid != null) {
				druid.setInitParameter("loginUsername", "druid");
				druid.setInitParameter("loginPassword", "druid123");
				druid.addMapping("/druid/*");
			}
		}
		
		if (!Environment.getProperty("spring.mvc.enable", Boolean.class, false)) {
			return;
		}
		
		Properties mvcProps = MvcPropertiesLoader.loadProperties();
		mvcProps.putAll(OtherPropertiesLoader.loadProperties());    //增加工程的属性
		ServletRegistration.Dynamic mvc = servletContext.addServlet(
				  	"mvc", new EnvirBootServlet(mvcProps));
		if (mvc != null) {
			mvc.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, "classpath:spring/spring-mvc.xml");
			mvc.setLoadOnStartup(0);
			mvc.addMapping(requestSuffix);
			System.out.println("    spring-mvc servlet added");
		}
	}
	
}
