package com.openxsl.config.dal.http;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.config.HttpPropertiesLoader;

/**
 * HttpClient调用服务(长连接)
 * @author xiongsl
 */
public final class HttpClientInvoker {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String charset = "UTF-8";
	private int readTimeout;
	
	private HttpClientBuilder builder;
	@Autowired(required=false)
	private PoolingHttpClientConnectionManager connectionManager;
	
	private static HttpClientInvoker instance;
	public static HttpClientInvoker getInstance() {
		Properties props = //PrefixProperties.get("commons-dal.properties", "http", false);
							new HttpPropertiesLoader().loadProperties();
		return getInstance(props);
	}
	public static synchronized HttpClientInvoker getInstance(Properties props) {
		if (instance == null) {
			String totalConns = props.getProperty("http.connect.total", "500");
			String eachConns = props.getProperty("http.connect.maxPerRoute", "100");
			String connTimeout = props.getProperty("http.connect.timeout", "2000");
			String readTimeout = props.getProperty("http.read.timeout", "5000");
			instance = new HttpClientInvoker( Integer.parseInt(totalConns),
								Integer.parseInt(eachConns),
								Integer.parseInt(connTimeout),
								Integer.parseInt(readTimeout)
						);
		}
		return instance;
	}
	
	public HttpClientInvoker(int totalConns, int eachConns, int connTimeout, int readTimeout){
		this.readTimeout = readTimeout;
		if (builder == null){
			builder = HttpClientBuilder.create();
			connectionManager = new PoolingHttpClientConnectionManager();
			connectionManager.setMaxTotal(totalConns);
	        connectionManager.setDefaultMaxPerRoute(eachConns);
	        builder.setConnectionManager(connectionManager);
//	        builder.setMaxConnTotal(totalConns);
//	        builder.setMaxConnPerRoute(eachConns);
	        RequestConfig requestConfig = RequestConfig.custom()
	        		.setConnectTimeout(connTimeout)
	        		.setSocketTimeout(readTimeout)
	        		.build();
	        builder.setDefaultRequestConfig(requestConfig)
	        		.setRetryHandler(new DefaultHttpRequestRetryHandler(0, true));
		}
	}
	
	//Spring传进来，可以触发调用链
	public HttpClientInvoker(HttpClientBuilder builder) {
		this.builder = builder;
//		this.connectionManager = builder.getConnectionManager();
	}
	
	public void addRequestInterceptor(HttpRequestInterceptor interceptor){
		builder.addInterceptorLast(interceptor);
	}
	public void addResponseInterceptor(HttpResponseInterceptor interceptor){
		builder.addInterceptorLast(interceptor);
	}
	
	/**
	 * 发送Http请求，返回String的响应内容
	 * 
	 * @param url  请求地址
	 * @param argsMap  参数对
	 * @param getOrPost 请求的方式：0-GET，1-POST
	 * @return String-content
	 */
	public String request(String url, Map<String,?> argsMap, int getOrPost)
				throws IOException{
		HttpResponse response = (getOrPost == 0) ? doGet(url, argsMap)
				  			  : doPost(url, argsMap);
		if (response != null){
			if (response.getEntity() != null){  //204
				return EntityUtils.toString(response.getEntity(), charset);
			}
		}
		return null;
	}
	public <T> T request(String url, Map<String,Object> argsMap, int getOrPost,
						 Class<T> returnType) throws IOException{
		String content = this.request(url, argsMap, getOrPost);
		logger.info("httpClient get content:{}", content);
		return JSON.parseObject(content, returnType);
	}
	
	public String postString(String url, String content, String contentType)throws IOException{
		HttpClient httpClient = this.getHttpClient();
		HttpPost method = new HttpPost(url);
		method.addHeader("Content-Type", contentType);
		method.setEntity(new StringEntity(content, charset));
		HttpResponse response = httpClient.execute(method);
		valideHttpResponse(response);
		String responseXml = EntityUtils.toString(response.getEntity(), charset);
		logger.info("response: {}", responseXml);
		return responseXml;
	}
	
	public String request(String[] urls, Map<String,Object> argsMap, int getOrPost)
				throws IOException{
		for (String url : urls){
			try{
				return this.request(url, argsMap, getOrPost);
			}catch(IOException e){
				logger.info("fail to request from url: {}", url);
			}
		}
		throw new IOException("no avaliable urls to finish request");
	}
	
	private HttpResponse doGet(String url, Map<String,?> argsMap) throws IOException{
		HttpClient httpClient = this.getHttpClient();
		if (argsMap != null){
			StringBuilder buffer = new StringBuilder(url);
			buffer.append(url.contains("?") ? "&" : "?");
			for (Map.Entry<String,?> entry : argsMap.entrySet()){
				buffer.append(entry.getKey()).append("=");
				String value = String.valueOf(entry.getValue());
				try{
					buffer.append(URLEncoder.encode(value, charset)).append("&");
				}catch(Exception e){
					buffer.append(value).append("&");
				}
			}
			url = buffer.toString();
		}
		
		HttpGet method = new HttpGet(url);
		HttpResponse response = httpClient.execute(method);
		valideHttpResponse(response);
		return response;
	}
	private HttpResponse doPost(String url, Map<String,?> argsMap) throws IOException{
		HttpClient httpClient = this.getHttpClient();
		HttpPost method = new HttpPost(url);
		if (argsMap != null){
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			for (Map.Entry<String,?> entry : argsMap.entrySet()){
				if (entry.getValue() != null){
					params.add(new BasicNameValuePair(entry.getKey(),
										String.valueOf(entry.getValue())));
				}
			}
			method.setEntity(new UrlEncodedFormEntity(params, charset));  //form
		}
		
		HttpResponse response = httpClient.execute(method);
		valideHttpResponse(response);
		return response;
	}
	
	private HttpClient getHttpClient(){
		return builder.build();
	}
	
	private static void valideHttpResponse(HttpResponse response) throws IOException{
		int httpCode = response.getStatusLine().getStatusCode();
		if (httpCode < 200 || httpCode >=300) {
			String error = response.getStatusLine().getReasonPhrase();
			StringBuilder buffer = new StringBuilder("Failed with HTTP code: ");
			buffer.append(httpCode).append(", error: ").append(error);
			throw new IOException(buffer.toString());
		}
	}
	
	private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread("IdleConnectionMonitor");
				}
			});
	public void startMonitor(){
		try{
			scheduler.scheduleAtFixedRate(
							new IdleConnectionEvictor(connectionManager, logger),
							readTimeout, readTimeout*10, TimeUnit.MILLISECONDS);
		}catch(IllegalStateException ise){
			logger.debug("task was already scheduled or cancelled.");
		}
	}
	public void stopMonitor(){
		scheduler.shutdown();
	}
	
	final class IdleConnectionEvictor extends TimerTask {
		private final PoolingHttpClientConnectionManager connectionManager;
		private final Logger logger;
		
		public IdleConnectionEvictor(PoolingHttpClientConnectionManager connectionManager,
						Logger logger) {
            this.connectionManager = connectionManager;
            this.logger = logger;
		}
 
        @Override
        public void run() {
        	if (connectionManager == null) {
        		return;
        	}
            // Close expired connections
            connectionManager.closeExpiredConnections();
            // Optionally, close Idle connections
            connectionManager.closeIdleConnections(readTimeout*10, TimeUnit.MILLISECONDS);
            if (logger.isDebugEnabled()) {
            	logger.debug("release end, available connections:{}",
            				connectionManager.getTotalStats().getAvailable());
            }
        }
    }

}
