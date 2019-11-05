package com.openxsl.tracing.registry.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ViewResolver;

import com.openxsl.config.tracing.service.RegistryService;
import com.openxsl.config.tracing.service.protocol.HttpRestRegistry;
import com.openxsl.tracing.registry.model.MvcWebApp;

/**
 * 注册SpringMVC rest服务
 * @author xiongsl
 * @modify 2018-12-21 没有必要注册每个uri，因为URL中的 namespace+contextpath就能唯一对应一个应用
 */
public class MvcRegistryInitializer extends AbstractRegistryInitializer<ViewResolver> {

	@Override
	public void onStartup(ServletContext sc) throws ServletException {
		System.out.println("Http-TracingInitializer start, order=500");
		super.onStartup(sc);
	}
	
	@Override
	protected WebApplicationContext findWebApplicationContext(ServletContext sc) {
		return ComponentsRegistryInitializer.findWebApplicationContext(sc, true);
	}

//	@Override
//	protected void register(HandlerMapping handlerMapping) {
//		Set<String> uriSet = new HashSet<String>();
//		if (handlerMapping instanceof RequestMappingHandlerMapping) {
//			RequestMappingHandlerMapping urlMapping = (RequestMappingHandlerMapping)handlerMapping;
//			Iterator<RequestMappingInfo> itr = urlMapping.getHandlerMethods().keySet().iterator();
//			while (itr.hasNext()) {
//				uriSet.addAll(itr.next().getPatternsCondition().getPatterns());
//			}
//		} else { //SimpleUrlHandlerMapping, BeanNameUrlHandlerMapping
//			AbstractUrlHandlerMapping urlMapping = (AbstractUrlHandlerMapping)handlerMapping;
//			uriSet.addAll(urlMapping.getHandlerMap().keySet());
//		}
//		uriSet.remove("/**");
//		
//		for (String restUri : uriSet) {
//			service.registerServer(new RestURL(restUri));
//		}
//	}
	protected void register(ViewResolver viewResolver) {
		service.registerServer(new MvcWebApp());
	}
	
	@Override
	protected String getThreadName() {
		return "SpringMvc-Registry";
	}

	@Override
	protected Class<? extends RegistryService> getServiceClass() {
		return HttpRestRegistry.class;
	}

}
