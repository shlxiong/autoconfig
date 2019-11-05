package com.openxsl.tracing.registry.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;

import com.openxsl.config.Environment;
import com.openxsl.tracing.registry.service.ComponentsRegistryService;

/**
 * Springboot注册Tracing组件
 * @author xiongsl
 */
@Order(PriorityOrdered.LOWEST_PRECEDENCE-1)
public class TracingRegistryBootListener implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (Environment.existSpringBoot() &&
				!Environment.exists("com.openxsl.config.boot.SumpayApplication")) {
			ComponentsRegistryService.register(event.getApplicationContext());
		}
	}

}
