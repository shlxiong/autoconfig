package com.openxsl.config.tracing.service.zookeeper;

import org.springframework.web.servlet.ViewResolver;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.tracing.service.protocol.HttpRestRegistry;
import com.openxsl.tracing.registry.model.MvcWebApp;
import com.openxsl.tracing.registry.model.Registration;

@ScanConfig("httpRegistry")
public class HttpRestRegistryImpl extends BaseZkRegistry implements HttpRestRegistry{

	@Override
	protected String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public Registration convertRegistra(Object bean) {
		return new MvcWebApp();
	}

	@Override
	public Class<?> getContextBeanType() {
		return ViewResolver.class;
	}

}
