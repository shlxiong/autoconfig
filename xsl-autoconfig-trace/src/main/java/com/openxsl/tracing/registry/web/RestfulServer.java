/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.openxsl.tracing.registry.web;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.ServletContext;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * REST API的内嵌服务器.
 *
 * @author zhangliang
 * @author caohao
 */
public final class RestfulServer {
	private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Server server;
    private final ServletContextHandler servletContextHandler;
    
    public RestfulServer(final int port, final String contextPath) {
        server = new Server(port);
        servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath(contextPath);
    }
    
    /**
     * 启动内嵌的RESTful服务器.
     * 
     * @param packages RESTful实现类所在包
     * @param resourcePath 资源路径
     * @throws Exception 启动服务器异常
     */
    public void start(final String packages, final Optional<String> resourcePath) throws Exception {
        start(packages, resourcePath, Optional.of("/api"));
    }
    
    /**
     * 启动内嵌的RESTful服务器.
     *
     * @param packages RESTful实现类所在包
     * @param resourcePath 资源路径
     * @param servletPath servlet路径
     * @throws Exception 启动服务器异常
     */
    public void start(final String packages, final Optional<String> resourcePath, final Optional<String> servletPath) throws Exception {
        logger.info("Elastic Job: Start RESTful server");
        HandlerList handlers = new HandlerList();
        if (resourcePath.isPresent()) {
            servletContextHandler.setBaseResource(Resource.newClassPathResource(resourcePath.get()));
            servletContextHandler.addServlet(new ServletHolder(DefaultServlet.class), "/*");
        }
        String servletPathStr = (servletPath.isPresent() ? servletPath.get() : "") + "/*";
        servletContextHandler.addServlet(getServletHolder(packages), servletPathStr);
        handlers.addHandler(servletContextHandler);
        server.setHandler(handlers);
        server.start();
    }
    
    /**
     * 添加Filter.
     *
     * @param filterClass filter实现类
     * @param urlPattern 过滤的路径
     * @return RESTful服务器
     */
    public RestfulServer addFilter(final Class<? extends Filter> filterClass, final String urlPattern) {
        servletContextHandler.addFilter(filterClass, urlPattern, EnumSet.of(DispatcherType.REQUEST));
        return this;
    }
    
    /**
     * TODO("XIONGSL") 获得Servlet上下文对象
     */
    public ServletContext getServletContext() {
    	return servletContextHandler.getServletContext();
    }
    
    private ServletHolder getServletHolder(final String packages) {
        ServletHolder result = new ServletHolder(ServletContainer.class);
        result.setInitParameter(PackagesResourceConfig.PROPERTY_PACKAGES, Joiner.on(",").join(RestfulServer.class.getPackage().getName(), packages));
        result.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", PackagesResourceConfig.class.getName());
        result.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", Boolean.TRUE.toString());
        result.setInitParameter("resteasy.scan.providers", Boolean.TRUE.toString());
        result.setInitParameter("resteasy.use.builtin.providers", Boolean.FALSE.toString());
        return result;
    }
    
    /**
     * 安静停止内嵌的RESTful服务器.
     * 
     */
    public void stop() {
        logger.info("Elastic Job: Stop RESTful server");
        try {
            server.stop();
            // CHECKSTYLE:OFF
        } catch (final Exception e) {
            // CHECKSTYLE:ON
            logger.error("Elastic Job: Stop RESTful server error", e);
        }
    }
}
