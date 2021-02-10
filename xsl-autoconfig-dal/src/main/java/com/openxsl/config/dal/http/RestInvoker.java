package com.openxsl.config.dal.http;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.openxsl.config.filter.ListableTracingFilter;
import com.openxsl.config.filter.TracingFilter;
import com.openxsl.config.util.KvPair;

/**
 * RestTemplate调用，调用spring-mvc推荐使用这个类
 * @author xiongsl
 * @modify 2018-11-30 增加TracingFilter处理，增加SSL处理
 */
public class RestInvoker implements InitializingBean, ApplicationContextAware{
	@Autowired
	private RestTemplate rest;
//	@Autowired
//	private org.apache.http.impl.client.HttpClientBuilder builder;
	private String charset = "UTF-8";
	private ListableTracingFilter filters = new ListableTracingFilter();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		ListableResponseErrorHandler errorHandlers = new ListableResponseErrorHandler();
		rest.setErrorHandler(errorHandlers);
		filters.load("http");  //http-trace-filter
		for (TracingFilter filter : filters.getFilters()) {
			if (filter instanceof ClientHttpRequestInterceptor) {
				this.addInterceptor((ClientHttpRequestInterceptor)filter);
			}
			if (filter instanceof ResponseErrorHandler) {
				errorHandlers.addErrorHandler((ResponseErrorHandler)filter);
			}
		}
	}

	/**
	 * POST表单提交文本信息（application/x-www-form-urlencoded）
	 * @param url
	 * @param queryString
	 * @return
	 */
	public String postForm(String url, String queryString){
        return this.postString(url, queryString, MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        						String.class);
	}
	
	/**
	 * POST提交JSON内容（application/json;charset=UTF-8）
	 */
	public String postJson(String url, String content){
        return this.postString(url, content, MediaType.APPLICATION_JSON_UTF8_VALUE, String.class);
	}
	
	public String postXml(String url, String content){
		return this.postString(url, content, MediaType.TEXT_XML_VALUE, String.class);
	}
	
	public final <T> T postString(String url, String content, String contentType, Class<T> returnType){
		T result = null;
		filters.before(url, "POST", content);
        try {
        	HttpHeaders headers = new HttpHeaders();
    		headers.add("Content-Type", contentType);
    		headers.add("Accept-Charset", charset);
            Object request = new HttpEntity<String>(content, headers);
            result = rest.postForObject(url, request, returnType);
            return result;
        } finally {
        	filters.after(result);
        }
	}
	
	/**
	 * POST表单提交KeyValue值（application/x-www-form-urlencoded）
	 */
	public String postForm(String url, KvPair... pairs){
		MultiValueMap<String,Object> map = new LinkedMultiValueMap<String,Object>();
		for (KvPair kv : pairs){
	        map.add(kv.getName(), kv.getValue());
		}
		String response = null;
		filters.before(url, "POST", map);
		try{
			response = rest.postForObject(url, map, String.class);
			return response;
		} finally{
			map.clear();
			filters.after(response);
		}
	}
	
	public <T> T get(String url, String content, String contentType, Class<T> returnType) {
		T result = null;
		filters.before(url, "GET", content);
        try {
        	HttpHeaders headers = new HttpHeaders();
    		headers.add("Content-Type", contentType);
    		headers.add("Accept-Charset", charset);
            Object request = new HttpEntity<String>(content, headers);
        	result = rest.getForObject(url, returnType, request);
        	return result;
        } finally{
        	filters.after(result);
		}
	}
	
	public void delete(String url, String content, String contentType) {
		filters.before(url, "DELETE", content);
        try {
        	HttpHeaders headers = new HttpHeaders();
    		headers.add("Content-Type", contentType);
    		headers.add("Accept-Charset", charset);
            Object request = new HttpEntity<String>(content, headers);
        	rest.delete(url, request);
        } finally{
        	filters.after(null);
		}
	}
	public void deleteForm(String url, KvPair... pairs){
		MultiValueMap<String,Object> map = new LinkedMultiValueMap<String,Object>();
		for (KvPair kv : pairs){
	        map.add(kv.getName(), kv.getValue());
		}
		filters.before(url, "DELETE", map);
		try{
			rest.delete(url, map);
		} finally{
			map.clear();
			filters.after(null);
		}
	}
	
	public void putForm(String url, KvPair... pairs){
		MultiValueMap<String,Object> map = new LinkedMultiValueMap<String,Object>();
		for (KvPair kv : pairs){
	        map.add(kv.getName(), kv.getValue());
		}
		filters.before(url, "PUT", map);
		try{
			rest.put(url, map);
		} finally{
			map.clear();
			filters.after(null);
		}
	}
	
	/** 添加拦截器将会在请求之前执行，可用于签名 */
	public void setInterceptors(List<ClientHttpRequestInterceptor> interceptors){
		rest.setInterceptors(interceptors);
	}
	public void addInterceptor(ClientHttpRequestInterceptor interceptor){
		rest.getInterceptors().add(interceptor);
	}
	/** 添加消息转换器，会在返回结果之前执行，可用于验签*/
	@Deprecated
	public void addResultConverter(HttpMessageConverter<?> converter){
		rest.getMessageConverters().add(converter);
	}
	public void replaceResultConverter(Class<? extends HttpMessageConverter<?>> type,
						HttpMessageConverter<?> converter){
		int idx = -1, i=0;
		for (HttpMessageConverter<?> older : rest.getMessageConverters()){
			if (older.getClass() == type){
				idx = i;
				break;
			}
			i++;
		}
		if (idx != -1){
			rest.getMessageConverters().remove(idx);
			rest.getMessageConverters().add(idx, converter);
		}
	}
	/**
	 * 添加Response异常处理器
	 * @param errorHandler
	 */
	public void addResponseHandler(ResponseErrorHandler errorHandler) {
		((ListableResponseErrorHandler)rest.getErrorHandler()).addErrorHandler(errorHandler);
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		filters.setSpringContext(applicationContext);
	}
	
	/**
	 * 处理返回结果
	 * @author xiongsl
	 */
	class ListableResponseErrorHandler implements ResponseErrorHandler{
		private List<ResponseErrorHandler> handlers = new ArrayList<ResponseErrorHandler>(2);
		
		ListableResponseErrorHandler(){
			handlers.add(new DefaultResponseErrorHandler());   //HttpStatus
		}
		
		public ListableResponseErrorHandler addErrorHandler(ResponseErrorHandler errorHandler) {
			handlers.add(errorHandler);
			return this;
		}

		@Override
		public boolean hasError(ClientHttpResponse response) throws IOException {
			return true;
		}

		@Override
		public void handleError(ClientHttpResponse response) throws IOException {
			for (ResponseErrorHandler handler : handlers) {
				if (handler.hasError(response)) {
					handler.handleError(response);
				}
			}
		}
		
	}
	
	/**
	 * Https安全协议   ==>org.apache.http.conn.ssl.NoopHostnameVerifier
	 * @author xiongsl
	 */
	//org.apache.http.conn.ssl.DefaultHostnameVerifier
	public static class InnerHostnameVerifier implements HostnameVerifier{
		private String certId;

//		@Override
//		public boolean verify(String host, SSLSession session) {
//			return true;
//		}
//
//		@Override
//		public void verify(String host, SSLSocket ssl) throws IOException {
//			// TODO Auto-generated method stub
//		}
//
//		@Override
//		public void verify(String host, X509Certificate cert) throws SSLException {
//			// TODO Auto-generated method stub
//		}
//
//		@Override
//		public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
//			// TODO Auto-generated method stub
//		}
		
		@Override
		public boolean verify(String hostname, SSLSession session) {
			if (certId!=null && certId.length()>0) {
				//TODO
			}
			return true;
		}
		public void setCertId(String certId) {
			this.certId = certId;
		}
	}
	public static class InnerSSLContextBuilder {
		static final TrustStrategy TRUST_STRATEGY = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		};
		public static SSLContext build() throws Exception {
			KeyStore keyStore = null;
			return new SSLContextBuilder().loadTrustMaterial(keyStore, TRUST_STRATEGY)
							.build();
		}
	}
	
	public static class SchemeRegistryBuilder {
		
		public static Registry<ConnectionSocketFactory> build() throws Exception {
			SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
						InnerSSLContextBuilder.build(), 
//						null, //new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"},
//						null,
						new InnerHostnameVerifier()
					);
	        return RegistryBuilder.<ConnectionSocketFactory>create()
	                .register("http", PlainConnectionSocketFactory.getSocketFactory())
	                .register("https", scsf)
	                .build();
	    }
	}
	
}
