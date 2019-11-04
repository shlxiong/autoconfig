package com.openxsl.config.autoconfig;

import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.openxsl.config.condition.ConditionalOnMissingBean;
import com.openxsl.config.condition.ConditionalOnPresent;

@Configuration
@ConditionalOnPresent(classes="org.springframework.web.client.RestTemplate")
public class RestTemplateConfiguration {
	@Value("${http.connect.timeout:2000}")
	private int connectTimeout;
	@Value("${http.read.timeout:5000}")
	private int readTimeout;
	
	@Bean
	@ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory requestFactory) {
        return new RestTemplate(requestFactory);
    }
	
    @Bean
    @ConditionalOnMissingBean(HttpComponentsClientHttpRequestFactory.class)
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = 
        			new HttpComponentsClientHttpRequestFactory(httpClient);
        httpRequestFactory.setConnectTimeout(connectTimeout);              // 连接超时
        httpRequestFactory.setReadTimeout(readTimeout);                    // 数据读取超时时间
        httpRequestFactory.setConnectionRequestTimeout(connectTimeout);    // 连接不够用的等待时间
        return httpRequestFactory;
    }

}
