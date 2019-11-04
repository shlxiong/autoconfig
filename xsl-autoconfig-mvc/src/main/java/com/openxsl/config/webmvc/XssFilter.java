package com.openxsl.config.webmvc;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * XSS（跨站点脚本攻击）过滤器，过滤掉html标签
 * @author xiongsl
 */
@WebFilter(filterName="xss-filter", urlPatterns={"*.jsp","*.html"})
public class XssFilter implements  Filter{
	
	 @Override  
	 public void init(FilterConfig filterConfig) throws ServletException{}   
	  
	 @Override  
	 public void doFilter(ServletRequest request, ServletResponse response,  
	            		FilterChain chain) throws IOException, ServletException {  
	     chain.doFilter(new XssHttpServletRequestWrapper(request), response);  
	 }  
	  
	 @Override  
	 public void destroy(){}    
	
}
