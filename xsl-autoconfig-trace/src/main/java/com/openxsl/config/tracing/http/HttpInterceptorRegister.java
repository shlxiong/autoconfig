package com.openxsl.config.tracing.http;

import java.util.List;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.openxsl.config.BootstrapApplication;
import com.openxsl.config.loader.GraceServiceLoader;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class HttpInterceptorRegister {
	private static final List<HttpSpiInterceptor> APACH_INTCPTS;
	private static final List<HttpSpiInterceptor> OK_INCPTS;
	
	static {
		APACH_INTCPTS = GraceServiceLoader.loadServices(HttpSpiInterceptor.class,
						"com.openxsl.config.tracing.http.HttpClientInterceptor");
		OK_INCPTS = GraceServiceLoader.loadServices(HttpSpiInterceptor.class,
						"com.openxsl.config.tracing.http.OkHttpInterceptor");
	}
	
	public static HttpClientBuilder registerTo(HttpClientBuilder builder) {
		for (HttpSpiInterceptor interceptor : APACH_INTCPTS) {
			builder.addInterceptorLast((HttpRequestInterceptor)interceptor);
			builder.addInterceptorLast((HttpResponseInterceptor)interceptor);
		}
		return builder;
	}
	public static HttpClient registerTo(HttpClient httpClient) {
		BootstrapApplication.getApplicationContext().getAutowireCapableBeanFactory()
					.autowireBean(httpClient);
		return httpClient;
	}
	
	public static OkHttpClient.Builder registerTo(OkHttpClient.Builder builder) {
		for (HttpSpiInterceptor interceptor : OK_INCPTS) {
			builder.addInterceptor((Interceptor)interceptor);
		}
		return builder;
	}

}
