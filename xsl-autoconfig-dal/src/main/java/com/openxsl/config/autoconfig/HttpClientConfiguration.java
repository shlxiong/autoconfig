package com.openxsl.config.autoconfig;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.openxsl.config.condition.ConditionalOnMissingBean;
import com.openxsl.config.condition.ConditionalOnPresent;
import com.openxsl.config.dal.RestInvoker.InnerSSLContextBuilder;

/**
 * 创建HttpClient对象
 * 
 * @author xiongsl
 */
@Configuration
@ConditionalOnPresent(classes="org.apache.http.impl.client.HttpClientBuilder")
//@ImportResource(locations="classpath*:spring/dal/http-client.xml")
//@Import({PrefixPropsRegistrar.class})
public class HttpClientConfiguration {
//	@PrefixProps(prefix="spring", regexp="(.*).http.(.*)")
//	private Properties httpProps;
	@Value("${http.connect.timeout:2000}")
	private int connectTimeout;
	@Value("${http.read.timeout:5000}")
	private int readTimeout;
	@Value("${http.connect.total:500}")
	private int total;
	@Value("${http.connect.maxPerRoute:100}")
	private int maxPertRoute;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Bean
    public HttpClient httpClient(HttpClientBuilder builder) {
    	return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(HttpClientBuilder.class)
    public HttpClientBuilder httpClientBuilder(HttpClientConnectionManager httpClientConnManager) {
    	HttpClientBuilder builder = HttpClientBuilder.create();
    	try {
    		RequestConfig requestConfig = RequestConfig.custom()
    						.setConnectTimeout(connectTimeout)
    						.setSocketTimeout(readTimeout)
    						.setConnectionRequestTimeout(connectTimeout)
    						.build();
	        builder.setConnectionManager(httpClientConnManager)
	        		.setDefaultRequestConfig(requestConfig);
	        builder.setRetryHandler(new DefaultHttpRequestRetryHandler(0, true)); //不重试
    	} catch (Exception e) {
        	logger.error("初始化HTTP连接池出错", e);
        }
    	return builder;
    }
    
    @Bean
    @ConditionalOnMissingBean(PoolingHttpClientConnectionManager.class)
    public PoolingHttpClientConnectionManager httpClientConnManager(
    				SSLContext sslContext, HostnameVerifier hostnameVerifier) {
    	Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
    			.<ConnectionSocketFactory>create()  // 注册http和https请求
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", new SSLConnectionSocketFactory(sslContext, hostnameVerifier))
                .build();
    	PoolingHttpClientConnectionManager connectionManager = 
    			new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    	connectionManager.setMaxTotal(total);           // 最大连接数500
        connectionManager.setDefaultMaxPerRoute(maxPertRoute); // 同路由并发数100
        return connectionManager;
    }
    
    @Bean
    @ConditionalOnMissingBean(SSLContext.class)
    public SSLContext sslContext() throws Exception {
    	return InnerSSLContextBuilder.build();
    }
    @Bean
    @ConditionalOnMissingBean(HostnameVerifier.class)
    public HostnameVerifier hostnameVerifier() {
    	return NoopHostnameVerifier.INSTANCE;
    }

}
