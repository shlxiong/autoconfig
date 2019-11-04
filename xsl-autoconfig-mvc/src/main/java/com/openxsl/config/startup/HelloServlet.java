package com.openxsl.config.startup;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 嗅探页面
 * @author xiongsl
 */
@SuppressWarnings("serial")
@WebServlet(name="welcome", urlPatterns= {"/index", "/hello"}, loadOnStartup=0)
public class HelloServlet extends HttpServlet{
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	        	throws ServletException, IOException {
		logger.info("Welcome! This is a test page.");
		
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().print("<H4>Hello, word!</H4>This is a test page.");
	}

}
