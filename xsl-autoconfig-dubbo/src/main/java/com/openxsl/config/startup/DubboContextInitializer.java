package com.openxsl.config.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.rpc.Protocol;
import com.openxsl.config.startup.autodetect.DubboMockServiceRegistry;

import com.openxsl.config.Environment;
import com.openxsl.config.util.Version;

/**
 * Dubbo启动类
 * @author xiongsl
 */
@Order(200)
public class DubboContextInitializer implements WebApplicationInitializer {

	@SuppressWarnings("resource")
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		if (Environment.existSpringBoot()) {
			return;
		}
		System.out.println("DubboContextInitializer start, order=200");
		
		Version.expectVersion(ApplicationConfig.class, "2.5.7", true);
		Version.expectVersion(ZooKeeper.class, "3.4.8", true);
		Version.expectVersionIfExist("org.I0Itec.zkclient.ZkClient", "0.7", true);
		Version.expectVersionIfExist("org.jboss.netty.bootstrap.ServerBootstrap", "3.10.5.Final", true);  //netty或netty-4
		
		new DubboMockServiceRegistry().refresh();
		
		//http或rest协议
		if (ExtensionLoader.getExtensionLoader(Protocol.class).hasExtension("rest")) {
			//servletContext.addListener("com.alibaba.dubbo.remoting.http.servlet.BootstrapListener");
			try {
				this.setServletManager(servletContext);
			}catch(Exception e) {
				throw new ServletException(e);
			}
			this.addDispatcherServlet("dubboRestServlet", servletContext);
		}else if (ExtensionLoader.getExtensionLoader(Protocol.class).hasExtension("http")) {
			this.addDispatcherServlet("dubboHttpServlet", servletContext);
		}
	}
	
	private void addDispatcherServlet(String name, ServletContext servletContext) {
		ServletRegistration.Dynamic dubbo = servletContext.addServlet(name,
					"com.alibaba.dubbo.remoting.http.servlet.DispatcherServlet");
		String contextPath = Environment.getProperty("dubbo.protocol.http.contextPath");
		if (contextPath == null) {
			contextPath = Environment.getProperty("spring.dubbo.protocol.http.contextPath");
		}
		contextPath = "/" + contextPath + "/*";
		dubbo.addMapping(contextPath);
		dubbo.setLoadOnStartup(2);
	}
	
	private void setServletManager(ServletContext servletContext) throws Exception {
		Class<?> managerClass = ClassHelper.forName("com.alibaba.dubbo.remoting.http.servlet.ServletManager");
    	Object manager = ReflectUtils.findMethodByMethodName(managerClass,"getInstance")
    						.invoke(managerClass.newInstance());
    	ReflectUtils.findMethodByMethodName(managerClass, "setStarterContext").invoke(manager, servletContext);
	}

}
