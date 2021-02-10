package com.openxsl.admin;

import javax.servlet.ServletContext;

/**
 * Important: registerFilter(springSecurityFilterChain)
 * springboot: SecurityFilterAutoConfiguration
 * 
 * @author shuilin.xiong
 *
 */
public class SecurityWebAppInitializer {//extends AbstractSecurityWebApplicationInitializer {
	
	/**
	 * Invoked before the "springSecurityFilterChain" is added.
	 * @param servletContext the {@link ServletContext}
	 */
	protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
	}

	/**
	 * Invoked after the springSecurityFilterChain is added.
	 * @param servletContext the {@link ServletContext}
	 */
	protected void afterSpringSecurityFilterChain(ServletContext servletContext) {

	}

}
