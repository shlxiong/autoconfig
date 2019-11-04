package com.openxsl.config.webmvc;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
	private final HttpServletRequest orgRequest;

	public XssHttpServletRequestWrapper(ServletRequest request) {
		super((HttpServletRequest)request);
		orgRequest = (HttpServletRequest)request;
	}

	/**
	 * 覆盖getParameter方法，将参数名和参数值都做xss过滤。<br/>
	 * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取<br/>
	 * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖
	 */
	@Override
	public String getParameter(String name) {
		String value = super.getParameter(xssEncode(name));
		if (value != null) {
			value = xssEncode(value);
		}
		return value;
	}
	
//	@Override
//	public String[] getParameterNames() {
//		String[] names = super.getParameterNames();
//	}
	
	@Override  
    public String getQueryString() {
		String value = super.getQueryString();
		if (value != null) {
			StringBuilder buffer = new StringBuilder();
			for (String pair : value.split("&")){
				buffer.append(xssEncode(pair)).append("&");
			}
			return buffer.toString();
		} else {
			return "";
		}
    }
	
	@Override  
    public String[] getParameterValues(String name) {  
        String[] values = super.getParameterValues(name);  
        if (values != null) {  
            int len = values.length;  
            for(int i = 0; i < len; i++){  
            	values[i] = xssEncode(values[i]);
            }  
        }  
        return values;  
    }
	
	@Override
	public Map<String,String[]> getParameterMap(){
		Enumeration<String> names = super.getParameterNames();
		Map<String,String[]> paramMap = new HashMap<String,String[]>();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			paramMap.put(xssEncode(name), this.getParameterValues(name));
		}
		return paramMap;
	}

	/**
	 * 覆盖getHeader方法，将参数名和参数值都做xss过滤。<br/>
	 * 如果需要获得原始的值，则通过super.getHeaders(name)来获取<br/>
	 * getHeaderNames 也可能需要覆盖
	 */
	@Override
	public String getHeader(String name) {
		String value = super.getHeader(xssEncode(name));
		if (value != null) {
			value = xssEncode(value);
		}
		return value;
	}

	/**
	 * 将容易引起xss漏洞的半角字符直接替换成全角字符
	 */
	private static String xssEncode(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		StringBuilder sb = new StringBuilder(s.length() + 16);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '>':
				sb.append('＞');// 全角大于号
				break;
			case '<':
				sb.append('＜');// 全角小于号
				break;
			case '\'':
				sb.append('‘');// 全角单引号
				break;
			case '\"':
				sb.append('“');// 全角双引号
				break;
			case '&':
				sb.append('＆');// 全角
				break;
			case '\\':
				sb.append('＼');// 全角斜线
				break;
			case '#':
				sb.append('＃');// 全角井号
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * 获取最原始的request
	 */
	public HttpServletRequest getOrgRequest() {
		return orgRequest;
	}

}
