package com.openxsl.tracing.registry.startup;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import com.openxsl.config.Environment;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.filter.tracing.TracingCollector;

/**
 * SpringApplicationRunListener(boot)
 * @author xiongsl
 */
@Order(PriorityOrdered.LOWEST_PRECEDENCE)
public class TracingSpringBootRunListener implements SpringApplicationRunListener {
	
	public TracingSpringBootRunListener(SpringApplication application, String[] args) {
	}

	@Override
	public void starting() {
	}

	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
	}

//	@Override  1.5.x的方法
	public void finished(ConfigurableApplicationContext context, Throwable exception) {
		if (Environment.existSpringBoot() &&
				!Environment.exists("com.openxsl.config.boot.OpenxslApplication")) {
			try {
				while (TraceContext.getRpcId().split("\\.").length >= 2){
					TraceContext.popStack();
				}
				TracingCollector.setT2();
			}catch (Throwable e) {
				System.err.printf("ERROR '%s' when tracing on refresh: %s\n", e.getMessage(),
								context.getId());
			}
		}
	}
	
	/**
	 * The context has been refreshed and the application has started but
	 * {@link CommandLineRunner CommandLineRunners} and {@link ApplicationRunner
	 * ApplicationRunners} have not been called.
	 * @param context the application context.
	 * @since 2.0.0
	 */
	public void started(ConfigurableApplicationContext context) {
		this.finished(context, null);
	}

	/**
	 * Called immediately before the run method finishes, when the application context has
	 * been refreshed and all {@link CommandLineRunner CommandLineRunners} and
	 * {@link ApplicationRunner ApplicationRunners} have been called.
	 * @param context the application context.
	 * @since 2.0.0
	 */
	public void running(ConfigurableApplicationContext context) {
		
	}

	/**
	 * Called when a failure occurs when running the application.
	 * @param context the application context or {@code null} if a failure occurred before
	 * the context was created
	 * @param exception the failure
	 * @since 2.0.0
	 */
	public void failed(ConfigurableApplicationContext context, Throwable exception) {
		
	}

}
