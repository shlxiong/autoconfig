package com.openxsl.config.startup;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.filter.tracing.TracingCollector;

public class TracingBootApplicationListener implements ApplicationListener<ApplicationContextEvent> {

	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			try {
				while (TraceContext.getRpcId().split("\\.").length >= 2){
					TraceContext.popStack();
				}
				TracingCollector.setT2();
			}catch (Throwable e) {
				System.err.printf("WARN: '%s' when tracing on refresh: %s\n", e.getMessage(),
								event.getApplicationContext().getId());
			}
		}
	}

}
