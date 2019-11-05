/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.openxsl.tracing.registry.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.http.util.IPAddress;
import com.alibaba.druid.support.http.util.IPRange;
import com.alibaba.druid.util.Utils;
import com.openxsl.config.util.StringUtils;

@WebServlet(name="resource", urlPatterns="/resource/*")
@SuppressWarnings("serial")
public class ResourceServlet extends HttpServlet {

    private static final Logger   LOG              = LoggerFactory.getLogger(ResourceServlet.class);

    public static final String PARAM_NAME_USERNAME = "loginUsername";
    public static final String PARAM_NAME_PASSWORD = "loginPassword";
    public static final String PARAM_NAME_ALLOW    = "allow";
    public static final String PARAM_NAME_DENY     = "deny";
    public static final String PARAM_REMOTE_ADDR   = "remoteAddress";

    protected String           username            = null;
    protected String           password            = null;

    protected List<IPRange>    allowList           = new ArrayList<IPRange>();
    protected List<IPRange>    denyList            = new ArrayList<IPRange>();

    protected final String     resourcePath;

    protected String           remoteAddressHeader = null;

    public ResourceServlet(/**String resourcePath*/){
        this.resourcePath = "META-INF/rest-web";
    }

    public void init() throws ServletException {
        initAuthEnv();
    }
    
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();
        if (contextPath == null) { // root context
            contextPath = "";
        }
        String uri = contextPath + servletPath;
        String path = requestURI.substring(uri.length());
        
        response.setCharacterEncoding("utf-8");

//        if (!isPermittedRequest(request)) {
//            path = "/nopermit.html";
//            returnResourceFile(path, uri, response);
//            return;
//        }

//        if ("/submitLogin".equals(path)) {
//            String usernameParam = request.getParameter(PARAM_NAME_USERNAME);
//            String passwordParam = request.getParameter(PARAM_NAME_PASSWORD);
//            if (username.equals(usernameParam) && password.equals(passwordParam)) {
//                request.getSession().setAttribute(SESSION_USER_KEY, username);
//                response.getWriter().print("success");
//            } else {
//                response.getWriter().print("error");
//            }
//            return;
//        }
//        if (isRequireAuth() //
//            && !ContainsUser(request)//
//            && !checkLoginParam(request)//
//            && !("/login.html".equals(path) //
//                 || path.startsWith("/css")//
//                 || path.startsWith("/js") //
//            || path.startsWith("/img"))) {
//            if (contextPath.equals("") || contextPath.equals("/")) {
//                response.sendRedirect("/druid/login.html");
//            } else {
//                if ("".equals(path)) {
//                    response.sendRedirect("druid/login.html");
//                } else {
//                    response.sendRedirect("login.html");
//                }
//            }
//            return;
//        }

        if ("/".equals(path)) {
            response.sendRedirect("index.html");
            return;
        }

        if (path.endsWith(".do") || path.endsWith(".htm")) {
        	this.process(contextPath + path);
        } else {
        	returnResourceFile(path, uri, request, response);
        }
    }

    private void initAuthEnv() {
        String paramUserName = getInitParameter(PARAM_NAME_USERNAME);
        if (!StringUtils.isEmpty(paramUserName)) {
            this.username = paramUserName;
        }

        String paramPassword = getInitParameter(PARAM_NAME_PASSWORD);
        if (!StringUtils.isEmpty(paramPassword)) {
            this.password = paramPassword;
        }

        String paramRemoteAddressHeader = getInitParameter(PARAM_REMOTE_ADDR);
        if (!StringUtils.isEmpty(paramRemoteAddressHeader)) {
            this.remoteAddressHeader = paramRemoteAddressHeader;
        }

        try {
            String param = getInitParameter(PARAM_NAME_ALLOW);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                String[] items = param.split(",");

                for (String item : items) {
                    if (item == null || item.length() == 0) {
                        continue;
                    }

                    IPRange ipRange = new IPRange(item);
                    allowList.add(ipRange);
                }
            }
        } catch (Exception e) {
            String msg = "initParameter config error, allow : " + getInitParameter(PARAM_NAME_ALLOW);
            LOG.error(msg, e);
        }

        try {
            String param = getInitParameter(PARAM_NAME_DENY);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                String[] items = param.split(",");

                for (String item : items) {
                    if (item == null || item.length() == 0) {
                        continue;
                    }

                    IPRange ipRange = new IPRange(item);
                    denyList.add(ipRange);
                }
            }
        } catch (Exception e) {
            String msg = "initParameter config error, deny : " + getInitParameter(PARAM_NAME_DENY);
            LOG.error(msg, e);
        }
    }

    public boolean isPermittedRequest(String remoteAddress) {
        boolean ipV6 = remoteAddress != null && remoteAddress.indexOf(':') != -1;
        if (ipV6) {
            return "0:0:0:0:0:0:0:1".equals(remoteAddress) || (denyList.size() == 0 && allowList.size() == 0);
        }

        IPAddress ipAddress = new IPAddress(remoteAddress);

        for (IPRange range : denyList) {
            if (range.isIPAddressInRange(ipAddress)) {
                return false;
            }
        }

        if (allowList.size() > 0) {
            for (IPRange range : allowList) {
                if (range.isIPAddressInRange(ipAddress)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    protected void returnResourceFile(String fileName, String uri,
    					HttpServletRequest request,HttpServletResponse response)
    			throws ServletException, IOException {
        String filePath;
        if (fileName.endsWith(".do") || fileName.endsWith(".htm")) {
        	filePath = request.getContextPath();
        	response.sendRedirect(filePath);
        	return;
        }

        filePath = resourcePath + fileName;
        if (filePath.endsWith(".html")) {
            response.setContentType("text/html; charset=utf-8");
        }
        if (fileName.endsWith(".jpg")) {
            byte[] bytes = Utils.readByteArrayFromResource(filePath);
            if (bytes != null) {
                response.getOutputStream().write(bytes);
            }
            return;
        }

        String text = this.readFromResource(filePath);
        if (text == null) {
            text = "404 - Not Found!";
        }
        if (fileName.endsWith(".css")) {
            response.setContentType("text/css;charset=utf-8");
        } else if (fileName.endsWith(".js")) {
            response.setContentType("text/javascript;charset=utf-8");
        }
        response.getWriter().write(text);
    }


//    public boolean isRequireAuth() {
//        return this.username != null;
//    }

    public boolean isPermittedRequest(HttpServletRequest request) {
        String remoteAddress = getRemoteAddress(request);
        return isPermittedRequest(remoteAddress);
    }

    protected String getRemoteAddress(HttpServletRequest request) {
        String remoteAddress = null;
        if (remoteAddressHeader != null) {
            remoteAddress = request.getHeader(remoteAddressHeader);
        }
        if (remoteAddress == null) {
            remoteAddress = request.getRemoteAddr();
        }
        return remoteAddress;
    }
    
    private String process(String url) {
    	return "";  //TODO
    }

    private final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private String readFromResource(String resource) throws IOException {
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (in == null) {
                in = getClass().getResourceAsStream(resource);
            }
            if (in == null) {
                return null;
            }
            
            StringWriter writer = new StringWriter();
            try (InputStreamReader reader = new InputStreamReader(in, "UTF-8")){
                char[] buffer = new char[DEFAULT_BUFFER_SIZE];
                int n = 0;
                while (-1 != (n = reader.read(buffer))) {
                    writer.write(buffer, 0, n);
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return writer.toString();
        } finally {
        	if (in != null) {
        		in.close();
        	}
        }
    }
}
