package com.openxsl.config.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import com.openxsl.config.Environment;

@Order(100)
public class RedisContextInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		if (Environment.existSpringBoot()) {
			return;
		}
		System.out.println("RedisContextInitializer start, order=100");
	}

}
